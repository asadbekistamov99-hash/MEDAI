package com.example.ui

import android.app.Application
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class SymptomQuestionsResult(
    val title: String = "",
    val medicalName: String = "",
    val questions: List<String> = emptyList()
)

// Single source of truth for the super-admin account. This is a client-side-only check
// (no server-side verification / Firebase custom claim yet) so it is not a real security
// boundary, but keeping it in one place avoids the email string drifting across files.
const val SUPER_ADMIN_EMAIL = "asadbekistamov99@gmail.com"

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val dao = database.appDao()

    // --- State Streams ---
    val currentUser = dao.getCurrentUserFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    // Re-derives from the live signed-in email on every read rather than trusting a stored
    // isAdmin flag, so it stays correct even if a Room row was edited/seeded incorrectly.
    val isSuperAdmin: Boolean
        get() {
            val user = try {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    dao.getCurrentUser()
                }
            } catch (t: Throwable) {
                currentUser.value
            } ?: currentUser.value

            return user?.email?.trim()?.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) == true
        }


    private var documentsJob: kotlinx.coroutines.Job? = null
    private var scansJob: kotlinx.coroutines.Job? = null
    private var lastInitializedUid: String? = null

    val appConfig = dao.getAppConfigFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppConfigLocal()
    )

    val symptomChecks = dao.getAllSymptomChecksFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders = dao.getAllRemindersFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val labResults = dao.getAllLabResultsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paymentRequests = dao.getAllPaymentRequestsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val familyMembers = dao.getAllFamilyMembersFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val immunizationRecords = dao.getAllImmunizationRecordsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications = dao.getAllNotificationsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount = dao.getUnreadNotificationsCountFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // --- New Features State Streams ---
    val todayMetrics = dao.getDailyMetricsFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allDailyMetrics = dao.getAllDailyMetricsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _firestoreReminders = MutableStateFlow<List<FirestoreMedicationReminder>>(emptyList())
    val firestoreReminders: StateFlow<List<FirestoreMedicationReminder>> = _firestoreReminders.asStateFlow()

    private val _firestoreVitals = MutableStateFlow<List<com.example.data.FirestoreVitalReading>>(emptyList())
    val firestoreVitals: StateFlow<List<com.example.data.FirestoreVitalReading>> = _firestoreVitals.asStateFlow()


    private val _medicalDocuments = MutableStateFlow<List<MedicalDocumentLocal>>(emptyList())
    val medicalDocuments: StateFlow<List<MedicalDocumentLocal>> = _medicalDocuments.asStateFlow()

    private val _prescriptionScans = MutableStateFlow<List<PrescriptionScanLocal>>(emptyList())
    val prescriptionScans: StateFlow<List<PrescriptionScanLocal>> = _prescriptionScans.asStateFlow()

    val appointmentRequests = dao.getAllAppointmentRequestsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val healthTips = dao.getAllHealthTipsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val systemUsers = dao.getAllSystemUsersFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userActivities = dao.getAllUserActivitiesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val errorLogs = dao.getAllErrorLogsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val apiUsageLogs = dao.getAllApiUsageLogsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val popupBannerConfig = dao.getPopupBannerConfigFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val announcementConfig = dao.getAnnouncementConfigFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val diseases = dao.getAllDiseasesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val medicines = dao.getAllMedicinesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appVersionConfig = dao.getAppVersionConfigFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val featureFlagsConfig = dao.getFeatureFlagsConfigFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val scheduledNotifications = dao.getAllScheduledNotificationsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val adminLogs = dao.getAllAdminLogsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var hasShownPopupThisSession = false


    private val _isCheckingInteractions = MutableStateFlow(false)
    val isCheckingInteractions: StateFlow<Boolean> = _isCheckingInteractions.asStateFlow()

    private val _interactionResult = MutableStateFlow("")
    val interactionResult: StateFlow<String> = _interactionResult.asStateFlow()

    private val _isScanningPrescription = MutableStateFlow(false)
    val isScanningPrescription: StateFlow<Boolean> = _isScanningPrescription.asStateFlow()

    private val _prescriptionScanResult = MutableStateFlow("")
    val prescriptionScanResult: StateFlow<String> = _prescriptionScanResult.asStateFlow()

    private val _isAnalyzingNutrition = MutableStateFlow(false)
    val isAnalyzingNutrition: StateFlow<Boolean> = _isAnalyzingNutrition.asStateFlow()

    private val _nutritionAdvice = MutableStateFlow("")
    val nutritionAdvice: StateFlow<String> = _nutritionAdvice.asStateFlow()


    // --- Selected UI Language State ---
    private val _currentLanguage = MutableStateFlow("uz")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // --- Onboarding Completion ---
    private val _onboardingCompleted = MutableStateFlow(true)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // --- Simulated GPS Location ---
    private val _gpsLocation = MutableStateFlow("41.311081, 69.240562") // Tashkent default
    val gpsLocation: StateFlow<String> = _gpsLocation.asStateFlow()

    // --- UI Dynamic States ---
    private val _aiTipsText = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiTipsText: StateFlow<Map<String, String>> = _aiTipsText.asStateFlow()

    private val _isLoadingTips = MutableStateFlow(false)
    val isLoadingTips: StateFlow<Boolean> = _isLoadingTips.asStateFlow()

    private val _isCheckingSymptoms = MutableStateFlow(false)
    val isCheckingSymptoms: StateFlow<Boolean> = _isCheckingSymptoms.asStateFlow()

    private val _symptomResultText = MutableStateFlow("")
    val symptomResultText: StateFlow<String> = _symptomResultText.asStateFlow()

    private val _isLoadingDrug = MutableStateFlow(false)
    val isLoadingDrug: StateFlow<Boolean> = _isLoadingDrug.asStateFlow()

    private val _drugInfoResult = MutableStateFlow<Map<String, String>?>(null)
    val drugInfoResult: StateFlow<Map<String, String>?> = _drugInfoResult.asStateFlow()

    private val _isLoadingLab = MutableStateFlow(false)
    val isLoadingLab: StateFlow<Boolean> = _isLoadingLab.asStateFlow()

    private val _labAnalysisResult = MutableStateFlow<String>("")
    val labAnalysisResult: StateFlow<String> = _labAnalysisResult.asStateFlow()

    // --- MedAI Yordamchi Dedicated States ---
    private val _pillIdentifyResult = MutableStateFlow<String>("")
    val pillIdentifyResult: StateFlow<String> = _pillIdentifyResult.asStateFlow()

    private val _isIdentifyingPill = MutableStateFlow(false)
    val isIdentifyingPill: StateFlow<Boolean> = _isIdentifyingPill.asStateFlow()

    private val _symptomQuestionsData = MutableStateFlow<SymptomQuestionsResult?>(null)
    val symptomQuestionsData: StateFlow<SymptomQuestionsResult?> = _symptomQuestionsData.asStateFlow()

    private val _isLoadingSymptomQuestions = MutableStateFlow(false)
    val isLoadingSymptomQuestions: StateFlow<Boolean> = _isLoadingSymptomQuestions.asStateFlow()

    private val _symptomDynamicAnalysisResult = MutableStateFlow("")
    val symptomDynamicAnalysisResult: StateFlow<String> = _symptomDynamicAnalysisResult.asStateFlow()

    private val _isAnalyzingSymptomAnswers = MutableStateFlow(false)
    val isAnalyzingSymptomAnswers: StateFlow<Boolean> = _isAnalyzingSymptomAnswers.asStateFlow()

    // --- UI Metrics Trackers (Analytics) - All start at 0 as requested ---
    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps.asStateFlow()

    private val _loggedWeight = MutableStateFlow(0.0)
    val loggedWeight: StateFlow<Double> = _loggedWeight.asStateFlow()

    private val _loggedSleep = MutableStateFlow(0.0)
    val loggedSleep: StateFlow<Double> = _loggedSleep.asStateFlow()

    private val _loggedWater = MutableStateFlow(0) // glasses
    val loggedWater: StateFlow<Int> = _loggedWater.asStateFlow()

    init {
        // Safe programmatic initialization of Firebase App
        try {
            if (com.google.firebase.FirebaseApp.getApps(application).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:835669cb29b74b1f:android:835669cb29b7")
                    .setProjectId("medai-uz-project")
                    .setApiKey("AIzaSyFakeKeyForLocalAndroidAppPreviewRun")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(application, options)
                Log.d("FirebaseInit", "Firebase App initialized programmatically successfully!")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase initialization bypassed/failed: ${e.message}")
        }
        viewModelScope.launch {
            // Prepopulate some app configuration if not already there
            val config = dao.getAppConfig()
            if (config == null) {
                dao.insertAppConfig(AppConfigLocal())
            }
            seedAdminDataIfNeeded()
            
            // Activate Premium VIP membership for current user and ensure admin authorization
            val existingUser = dao.getCurrentUser()
            if (existingUser != null) {
                val shouldBeAdmin = existingUser.email.trim().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
                var updatedUser = existingUser
                if (!existingUser.isPremium) {
                    val oneYearExpiry = System.currentTimeMillis() + 365L * 24 * 3600 * 1000L
                    updatedUser = updatedUser.copy(isPremium = true, premiumExpiry = oneYearExpiry)
                }
                if (existingUser.isAdmin != shouldBeAdmin) {
                    updatedUser = updatedUser.copy(isAdmin = shouldBeAdmin)
                }
                // When resetting statistics as requested, bring score to 0 baseline if no activities completed yet
                dao.insertUser(updatedUser)
                recalculateHealthScore()
            } else {
                // If no user exists, create a default active Premium user profile for instant access with 0 baseline
                val defaultUid = UUID.randomUUID().toString()
                val defaultUser = UserLocal(
                    uid = defaultUid,
                    name = "Asadbek Istamov",
                    email = SUPER_ADMIN_EMAIL,
                    phone = "+998 90 123 45 67",
                    dateOfBirth = "1999-05-15",
                    gender = "male",
                    bloodType = "O+",
                    height = 178.0,
                    weight = 72.0,
                    isPremium = true,
                    premiumExpiry = System.currentTimeMillis() + 365L * 24 * 3600 * 1000L,
                    language = "uz",
                    fcmToken = "fcm_token_" + defaultUid.take(6),
                    createdAt = System.currentTimeMillis(),
                    lastActive = System.currentTimeMillis(),
                    healthScore = 0, // Starts strictly at 0 for all new users
                    isAdmin = true,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80"
                )
                dao.insertUser(defaultUser)
                _onboardingCompleted.value = true
                addMockFamilyData(defaultUid)
            }
            
            checkPremiumExpiriesAndProcess()


            // Prepopulate default health tips if empty
            val currentTips = dao.getAllHealthTipsFlow().first()
            if (currentTips.isEmpty()) {
                dao.insertHealthTip(HealthTipLocal(
                    uz = "Har kuni 8 stakan suv iching 💧",
                    ru = "Пейте 8 стаканов воды каждый день 💧",
                    en = "Drink 8 glasses of water every day 💧",
                    orderIndex = 0
                ))
                dao.insertHealthTip(HealthTipLocal(
                    uz = "Ertalab 10 daqiqa yurish yurak sog'lig'ini yaxshilaydi 🚶",
                    ru = "10-минутная утренняя прогулка улучшает здоровье сердца 🚶",
                    en = "A 10-minute morning walk improves heart health 🚶",
                    orderIndex = 1
                ))
                dao.insertHealthTip(HealthTipLocal(
                    uz = "7-9 soat uyqu immunitetni mustahkamlaydi 😴",
                    ru = "7-9 часов сна укрепляют иммунитет 😴",
                    en = "7-9 hours of sleep strengthens immunity 😴",
                    orderIndex = 2
                ))
            }

            // Observe current user to set initial language and load user-specific data
            currentUser.collect { user ->
                if (user != null) {
                    _currentLanguage.value = user.language
                    _onboardingCompleted.value = true

                    if (user.uid != lastInitializedUid) {
                        lastInitializedUid = user.uid

                        startFirestoreRemindersListener(user.uid)
                        startFirestoreVitalsListener(user.uid)

                        val isAdminUser = user.email.trim().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
                        if (isAdminUser) {
                            startAdminLogsFirestoreListener()
                        }
                        startPaymentRequestsFirestoreListener(user.uid, isAdminUser)

                        // Load user-specific documents and prescription scans safely
                        documentsJob?.cancel()
                        documentsJob = launch(Dispatchers.IO) {
                            dao.getAllMedicalDocumentsFlow(user.uid).collect { docs ->
                                _medicalDocuments.value = docs
                            }
                        }
                        scansJob?.cancel()
                        scansJob = launch(Dispatchers.IO) {
                            dao.getAllPrescriptionScansFlow(user.uid).collect { scans ->
                                _prescriptionScans.value = scans
                            }
                        }
                    }
                }
            }
        }

        // Periodic health state sync (no fake step increments, all stats real from 0 baseline)
        viewModelScope.launch {
            while (true) {
                delay(30000) // check every 30 seconds
                val userObj = currentUser.value
                if (userObj != null) {
                    recalculateHealthScore()
                }

                // Simulate family members dynamic health updates to reflect REAL-TIME in UI
                val list = familyMembers.value
                if (list.isNotEmpty()) {
                    val updated = list.map { member ->
                        val stepChange = (5..20).random()
                        val scoreShift = (-2..2).random()
                        val newScore = (member.healthScore + scoreShift).coerceIn(30, 100)
                        
                        // Auto notify if member health falls below 40 as per push triggers specs
                        if (newScore < 40 && member.healthScore >= 40 && member.inviteStatus == "accepted") {
                            sendSimulatedPush(
                                "⚠️ ${member.name} sog'lig'i yomonlashdi",
                                "${member.name}ning salomatlik ko'rsatkichi $newScore gacha pasaydi. Iltimos, nazorat qiling.",
                                "sos"
                            )
                        }

                        member.copy(
                            stepsToday = member.stepsToday + stepChange,
                            healthScore = newScore,
                            lastActive = System.currentTimeMillis()
                        )
                    }
                    updated.forEach { dao.insertFamilyMember(it) }
                }
            }
        }
    }

    // --- Authentication ---
    suspend fun registerUserSuspend(
        name: String, email: String, phone: String, dob: String, gender: String,
        bloodType: String, height: Double, weight: Double
    ) {
        val uid = UUID.randomUUID().toString()
        val user = UserLocal(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            dateOfBirth = dob,
            gender = gender,
            bloodType = bloodType,
            height = height,
            weight = weight,
            isPremium = true,
            premiumExpiry = System.currentTimeMillis() + 365L * 24 * 3600 * 1000L,
            language = _currentLanguage.value,
            fcmToken = "simulated_fcm_token_" + UUID.randomUUID().toString().take(6),
            createdAt = System.currentTimeMillis(),
            lastActive = System.currentTimeMillis(),
            healthScore = 0, // Starts at 0 for all new users; grows as rules & medical tasks are completed
            isAdmin = email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true),
            isBanned = false,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80"
        )
        dao.insertUser(user)
        _onboardingCompleted.value = true
        
        // Add a welcome notification
        dao.insertNotification(NotificationLocal(
            userId = uid,
            title = "MedAI xush kelibsiz! 🎉",
            message = "Sizning AI shaxsiy tibbiy yordamchingiz ishga tushdi. Salomatligingizni bugundan boshlab yaxshilang.",
            type = "system"
        ))

        // Add mock family members for simulation
        addMockFamilyData(uid)
    }

    fun registerUser(
        name: String, email: String, phone: String, dob: String, gender: String,
        bloodType: String, height: Double, weight: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            registerUserSuspend(name, email, phone, dob, gender, bloodType, height, weight)
            onSuccess()
        }
    }

    fun loginUser(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val shouldBeAdmin = trimmedEmail.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
            // Attempt to login. If exists, we can use it. Otherwise, create a default user profile.
            val current = dao.getCurrentUser()
            if (current != null && current.email.equals(trimmedEmail, ignoreCase = true)) {
                dao.insertUser(current.copy(
                    lastActive = System.currentTimeMillis(),
                    isAdmin = shouldBeAdmin
                ))
            } else {
                registerUserSuspend(
                    name = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = trimmedEmail,
                    phone = "+998 90 123 45 67",
                    dob = "1999-05-15",
                    gender = "male",
                    bloodType = "O+",
                    height = 178.0,
                    weight = 72.0
                )
            }
            onSuccess()
        }
    }

    fun loginWithGoogle(name: String, email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            dao.clearCurrentUser()
            registerUserSuspend(
                name = name,
                email = email,
                phone = "+998 99 999 99 99",
                dob = "1998-08-20",
                gender = "male",
                bloodType = "A+",
                height = 175.0,
                weight = 68.0
            )
            if (email.trim().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)) {
                ensureAdminClaimIfEligible()
            }
            onSuccess()
        }
    }

    // Requests the server-side admin custom claim (see functions/index.js) for the
    // currently signed-in Firebase Auth user, when it's the configured admin account. This is
    // what actually makes firestore.rules' isAdmin() check pass — the local isSuperAdmin
    // property alone only controls what the UI shows, it has no server-side effect. Safe to
    // call defensively (e.g. every admin-panel open): it no-ops once the claim is already set,
    // and fails silently (logged only) if Cloud Functions aren't deployed yet.
    fun ensureAdminClaimIfEligible() {
        val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        if (!(fbUser.email?.trim()?.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) == true)) return
        com.google.firebase.functions.FirebaseFunctions.getInstance()
            .getHttpsCallable("claimAdminIfEligible")
            .call()
            .addOnSuccessListener {
                Log.d("AdminClaim", "Server-side admin claim confirmed for ${fbUser.email}")
            }
            .addOnFailureListener { e ->
                Log.w("AdminClaim", "Could not confirm server-side admin claim (functions not deployed yet?): ${e.message}")
            }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.e("Auth", "Failed to signOut Firebase Auth: ${e.message}")
            }
            try {
                remindersListenerRegistration?.remove()
                remindersListenerRegistration = null
                vitalsListenerRegistration?.remove()
                vitalsListenerRegistration = null
            } catch (e: Exception) {
                Log.e("Auth", "Failed to remove listeners: ${e.message}")
            }
            _firestoreReminders.value = emptyList()
            _firestoreVitals.value = emptyList()
            dao.clearCurrentUser()
            _onboardingCompleted.value = false
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        viewModelScope.launch {
            val user = dao.getCurrentUser()
            if (user != null) {
                dao.insertUser(user.copy(language = lang))
            }
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }

    // --- Health Metrics Logging ---
    fun logWeight(weight: Double) {
        _loggedWeight.value = weight
        viewModelScope.launch {
            val user = dao.getCurrentUser()
            if (user != null) {
                dao.insertUser(user.copy(weight = weight))
            }
        }
    }

    fun logSleep(hours: Double) {
        _loggedSleep.value = hours
    }

    fun logWater(glasses: Int) {
        _loggedWater.value = glasses
    }

    // --- Symptom Checker Feature ---
    fun checkSymptoms(symptoms: String, bodyPart: String, duration: String = "", severity: String = "") {
        viewModelScope.launch {
            _isCheckingSymptoms.value = true
            _symptomResultText.value = ""

            val userObj = currentUser.value
            val lang = _currentLanguage.value

            val localDiseases = dao.getAllDiseasesFlow().first()
            val localDiseasesContext = if (localDiseases.isNotEmpty()) {
                "Local reference database of medical conditions to consider when analyzing:\n" +
                localDiseases.joinToString("\n") {
                    "- ${it.nameUz} (${it.nameEn}). Symptoms: ${it.symptomsUz}. Description: ${it.descriptionUz} (Severity: ${it.severity}, Specialist: ${it.specialistType})"
                } + "\n\n"
            } else ""

            val durationText = if (duration.isNotEmpty()) "Duration: $duration" else "Duration: not specified"
            val severityText = if (severity.isNotEmpty()) "Severity: $severity" else "Severity: not specified"

            val prompt = """
                You are a highly detailed professional medical diagnostic assistant. Provide an initial health assessment and recommended next steps based on the user's symptoms.
                
                User Profile Context:
                - Age/DOB: ${userObj?.dateOfBirth ?: "Not specified"}
                - Gender: ${userObj?.gender ?: "Not specified"}
                - Blood Type: ${userObj?.bloodType ?: "Not specified"}
                - Weight: ${userObj?.weight ?: "Not specified"} kg
                - Height: ${userObj?.height ?: "Not specified"} cm
                
                Symptom Details:
                - Symptoms: $symptoms
                - Targeted Body Area: $bodyPart
                - $durationText
                - $severityText
                
                Please generate an assessment in $lang language.
                The response must be structured beautifully with markdown. Make sure it has:
                
                1. 🩺 **Boshlang'ich Salomatlik Baholash (Initial Health Assessment)**:
                   - Analyze the symptoms, severity, and duration.
                   - Provide possible causes/conditions and explain their mechanisms in accessible, professional language.
                   - Mention if the duration and severity combination is typical or if it signifies any acute/chronic patterns.
                
                2. 📋 **Tavsiya Etilgan Keyingi Qadamlar (Recommended Next Steps)**:
                   - Recommend specific medical specialists to consult (e.g., Cardiologist, Neurologist, General Practitioner).
                   - Suggest highly relevant diagnostic tests or labs (e.g., Blood tests, MRI, X-ray) that could help confirm the diagnosis.
                   - Offer safe, standard self-care, symptom relief, or home remedy advice while waiting for an appointment.
                   - State clear "red flags" (emergency symptoms) that require immediate emergency medical care (ER).
                
                3. ⚠️ **Muhim Ogohlantirish (Medical Disclaimer)**:
                   - Remind the user that this is an AI-powered educational assessment and does not replace professional clinical judgment or physical exams.
            """.trimIndent()

            val systemInstruction = "You are a professional medical diagnostic assistant. Communicate in $lang."
            val response = GeminiClient.generateText(localDiseasesContext + prompt, systemInstruction)

            _symptomResultText.value = response
            _isCheckingSymptoms.value = false

            // Save check in History and Logs
            if (userObj != null) {
                val fullInput = if (duration.isNotEmpty() || severity.isNotEmpty()) {
                    "$symptoms [Davomiyligi: $duration, Og'irligi: $severity]"
                } else {
                    symptoms
                }

                dao.insertUserActivity(UserActivityLog(
                    userId = userObj.uid,
                    screenName = "SymptomChecker",
                    actionType = "symptom_check",
                    details = "Sintomlar tekshirildi: $symptoms ($bodyPart), Davomiyligi: $duration, Og'irligi: $severity"
                ))
                dao.insertApiUsageLog(ApiUsageLog(
                    feature = "Symptom",
                    tokensUsed = 1800,
                    costEstimateUsd = 0.0036
                ))

                dao.insertSymptomCheck(
                    SymptomCheck(
                        userId = userObj.uid,
                        symptomsInput = fullInput,
                        bodyPart = bodyPart,
                        resultJson = response
                    )
                )
            }
        }
    }

    // --- AI Doctor Chat ---
    fun sendChatMessage(message: String, chatType: String = "doctor") {
        viewModelScope.launch {
            val userObj = currentUser.value ?: return@launch
            
            // Limit general chat for free users
            if (chatType == "general" && !userObj.isPremium) {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                val count = dao.getTodayGeneralChatCount(startOfDay)
                val config = dao.getAppConfig() ?: AppConfigLocal()
                if (count >= config.maxFreeChatMessages) {
                    Toast.makeText(getApplication(), "Kunning bepul xabarlar limiti tugadi (maks $count)", Toast.LENGTH_LONG).show()
                    return@launch
                }
            }

            // Save user message
            val userMsg = ChatMessageLocal(userId = userObj.uid, chatType = chatType, role = "user", content = message)
            dao.insertChatMessage(userMsg)

            // Prompt prep
            val lang = _currentLanguage.value
            val systemIns = if (chatType == "doctor") {
                "You are MedAI Doctor, a professional empathetic medical specialist. Respond in $lang. Address user questions scientifically yet understandably. Always recommend seeing a real doctor for serious issues."
            } else {
                "You are MedAI, a general health and lifestyle assistant. Respond in $lang. Help users with diet, fitness, hydration, and general queries."
            }

            // Simulate typing indicator
            delay(1000)

            val fullHistoryFlow = dao.getChatMessagesFlow(chatType).first()
            val conversationString = fullHistoryFlow.takeLast(10).joinToString("\n") { "${it.role}: ${it.content}" }
            val prompt = "$conversationString\nuser: $message\nmodel: "

            val modelResponse = GeminiClient.generateText(prompt, systemIns)

            val modelMsg = ChatMessageLocal(userId = userObj.uid, chatType = chatType, role = "model", content = modelResponse)
            dao.insertChatMessage(modelMsg)
        }
    }

    fun clearChatHistory(chatType: String) {
        viewModelScope.launch {
            dao.clearChatHistory(chatType)
        }
    }

    // --- AI Daily Advice (Premium only) ---
    fun fetchPersonalizedTips() {
        viewModelScope.launch {
            _isLoadingTips.value = true
            val userObj = currentUser.value ?: return@launch
            val lang = _currentLanguage.value

            val prompt = """
                Based on this profile: Age: ${userObj.dateOfBirth}, Gender: ${userObj.gender}, Height: ${userObj.height}cm, Weight: ${userObj.weight}kg.
                Generate personalized health advice for today in $lang.
                
                You must return exactly 4 sections in plain text separated by the keyword "---":
                Section 1: Ovqatlanish (Nutrition tip)
                Section 2: Jismoniy faoliyat (Fitness advice)
                Section 3: Uyqu (Sleep optimization tip)
                Section 4: Ruhiy salomatlik (Mental wellness tip)
                Keep each tip clear, actionable, and 2-3 sentences.
            """.trimIndent()

            val response = GeminiClient.generateText(prompt, "You are a professional clinical nutritionist and wellness couch.")
            val split = response.split("---")
            if (split.size >= 4) {
                _aiTipsText.value = mapOf(
                    "nutrition" to split[0].trim(),
                    "activity" to split[1].trim(),
                    "sleep" to split[2].trim(),
                    "mental" to split[3].trim()
                )
            } else {
                _aiTipsText.value = mapOf(
                    "nutrition" to response,
                    "activity" to "Bugun ko'proq qadam bosing. Kamida 8000 qadam yurish tavsiya etiladi.",
                    "sleep" to "Uyqudan 2 soat oldin telefon ekranini o'chiring va xonani shamollating.",
                    "mental" to "5 daqiqa davomida nafas mashqlarini bajaring: 4 soniya nafas oling, 4 soniya ushlab turing, 4 soniya chiqaring."
                )
            }
            _isLoadingTips.value = false
        }
    }

    // --- Drug Information (Free & Premium) ---
    fun searchDrugInfo(drugName: String) {
        viewModelScope.launch {
            _isLoadingDrug.value = true
            _drugInfoResult.value = null
            val lang = _currentLanguage.value
            val isPremium = currentUser.value?.isPremium ?: false

            // Check local DB first
            val localMed = dao.findMedicine("%$drugName%")
            if (localMed != null) {
                val resultMap = mapOf(
                    "Dosage" to localMed.dosage,
                    "SideEffects" to localMed.sideEffects,
                    "Interactions" to "Ma'lumot mavjud emas (Mahalliy bazadan o'qildi)",
                    "Contraindications" to "Ma'lumotlar ombordan yuklandi",
                    "Alternatives" to localMed.category,
                    "Description" to localMed.description
                )
                _drugInfoResult.value = resultMap
                _isLoadingDrug.value = false

                val user = currentUser.value
                if (user != null) {
                    dao.insertUserActivity(UserActivityLog(
                        userId = user.uid,
                        screenName = "MedicineSearch",
                        actionType = "medicine_search",
                        details = "Dori qidirildi: $drugName (Mahalliy bazadan topildi)"
                    ))
                }
                return@launch
            }

            val user = currentUser.value
            if (user != null) {
                dao.insertUserActivity(UserActivityLog(
                    userId = user.uid,
                    screenName = "MedicineSearch",
                    actionType = "medicine_search",
                    details = "Dori qidirildi: $drugName (AI orqali qidirildi)"
                ))
                // Log API usage
                dao.insertApiUsageLog(ApiUsageLog(
                    feature = "Medicine",
                    tokensUsed = 1200,
                    costEstimateUsd = 0.0024
                ))
            }

            val prompt = if (isPremium) {
                """
                Provide detailed professional medical information about the drug "$drugName" in $lang.
                You must return a structured response with these labels separated by '|':
                Dosage: [details]
                SideEffects: [details]
                Interactions: [details]
                Contraindications: [details]
                Alternatives: [details]
                PregnancySafety: [details]
                Storage: [details]
                """.trimIndent()
            } else {
                """
                Provide basic medical information about the drug "$drugName" in $lang.
                You must return a structured response with these labels separated by '|':
                Dosage: [details]
                SideEffects: [details]
                Interactions: [interactions or 'Premium exclusive']
                Contraindications: [contraindications or 'Premium exclusive']
                Alternatives: [alternatives or 'Premium exclusive']
                """.trimIndent()
            }

            val response = GeminiClient.generateText(prompt, "You are a pharmacology expert assistant.")
            val parts = response.split("|")
            val resultMap = mutableMapOf<String, String>()
            
            parts.forEach { part ->
                val pair = part.split(":", limit = 2)
                if (pair.size == 2) {
                    resultMap[pair[0].trim()] = pair[1].trim()
                }
            }

            if (resultMap.isEmpty()) {
                resultMap["Dosage"] = response
                resultMap["SideEffects"] = "Shifokor ko'rsatmasi bo'yicha qo'llang."
            }

            _drugInfoResult.value = resultMap
            _isLoadingDrug.value = false
        }
    }

    // --- MedAI Yordamchi Dedicated Methods ---

    fun identifyPill(pillName: String) {
        viewModelScope.launch {
            _isIdentifyingPill.value = true
            _pillIdentifyResult.value = ""
            val lang = _currentLanguage.value

            val prompt = """
                Dori nomi: "$pillName".
                Ushbu dori haqida to'liq va aniq tibbiy yoriqnoma tayyorlang ($lang tilida).
                
                Tuzilishi:
                1. 💊 Asosiy tarkibi va ta'siri (Farmakologik xususiyatlari)
                2. ⏱️ Qo'llash usuli va dozalari (kattalar, bolalar, ovqatdan oldin/keyin)
                3. ⚠️ Nojo'ya ta'sirlari va ehtiyot choralari
                4. 🚫 Qarshi ko'rsatmalar (kimlarga mumkin emas)
                5. 🔄 Boshqa dorilar bilan o'zaro ta'siri
                6. 🌡️ Saqlash sharoiti.
                
                Matnni juda tushunarli, chiroyli va o'qishli formatda bering.
            """.trimIndent()

            val response = GeminiClient.generateText(prompt, "Siz tajribali klinik farmakolog shifokorsiz.")
            _pillIdentifyResult.value = response
            _isIdentifyingPill.value = false

            val user = currentUser.value
            if (user != null) {
                dao.insertUserActivity(UserActivityLog(
                    userId = user.uid,
                    screenName = "PillIdentify",
                    actionType = "pill_identify",
                    details = "Dori yorig'i olindi: $pillName"
                ))
            }
        }
    }

    fun getSymptomQuestions(complaint: String) {
        viewModelScope.launch {
            _isLoadingSymptomQuestions.value = true
            _symptomQuestionsData.value = null
            _symptomDynamicAnalysisResult.value = ""
            val lang = _currentLanguage.value

            val prompt = """
                Foydalanuvchi quyidagi shikoyatni kiritdi: "$complaint".
                Ushbu simptomni aniqroq tahlil qilish va tashxis qo'yish uchun shifokor berishi kerak bo'lgan 3 tadan 5 tagacha eng muhim klinik savollarni shakllantiring ($lang tilida).
                Shuningdek, simptomning qisqa nomini (title) va tibbiy/ilmiy nomini (medical_name) aniqlang.
                
                Javobni FAQAT toza JSON formatida bering:
                {
                  "title": "Bosh og'rig'i",
                  "medical_name": "Cephalea",
                  "questions": [
                    "Og'riq boshning qaysi sohasida ko'proq sezilmoqda?",
                    "Og'riq qachon boshlangan va qanday xarakterga ega (o'tkir, simillovchi, bosuvchi)?",
                    "Ko'ngil aynishi, ko'z xiralashishi yoki bosh aylanishi bormi?"
                  ]
                }
            """.trimIndent()

            val response = GeminiClient.generateText(prompt, "Siz professional diagnostik shifokorsiz. Faqat to'g'ri JSON qaytaring.")
            try {
                val startIdx = response.indexOf("{")
                val endIdx = response.lastIndexOf("}")
                if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                    val jsonStr = response.substring(startIdx, endIdx + 1)
                    val json = org.json.JSONObject(jsonStr)
                    val title = json.optString("title", complaint.take(25))
                    val medName = json.optString("medical_name", "Klinik simptom")
                    val qArray = json.optJSONArray("questions")
                    val questions = mutableListOf<String>()
                    if (qArray != null) {
                        for (i in 0 until qArray.length()) {
                            val qText = qArray.optString(i).trim()
                            if (qText.isNotEmpty()) questions.add(qText)
                        }
                    }
                    if (questions.isEmpty()) {
                        questions.add("Belgilar qachondan beri bezovta qilmoqda?")
                        questions.add("Tana harorati ko'tarilganmi?")
                        questions.add("Avval ham shunday holat kuzatilganmi?")
                    }
                    _symptomQuestionsData.value = SymptomQuestionsResult(title, medName, questions)
                } else {
                    _symptomQuestionsData.value = SymptomQuestionsResult(
                        title = complaint.take(25),
                        medicalName = "Simptom tekshiruvi",
                        questions = listOf(
                            "Belgilar qachondan beri bezovta qilmoqda?",
                            "Og'riq yoki noqulaylik darajasi (1-10) qanday?",
                            "Harorat, ko'ngil aynishi kabi qo'shimcha belgilar bormi?"
                        )
                    )
                }
            } catch (e: Exception) {
                _symptomQuestionsData.value = SymptomQuestionsResult(
                    title = complaint.take(25),
                    medicalName = "Klinik simptom",
                    questions = listOf(
                        "Belgilar qachon boshlandi?",
                        "Og'riq qanday xarakterga ega?",
                        "Qanday qo'shimcha alomatlar bor?"
                    )
                )
            }
            _isLoadingSymptomQuestions.value = false
        }
    }

    fun analyzeSymptomAnswers(complaint: String, questions: List<String>, answers: List<String>) {
        viewModelScope.launch {
            _isAnalyzingSymptomAnswers.value = true
            _symptomDynamicAnalysisResult.value = ""
            val lang = _currentLanguage.value

            val qaList = questions.mapIndexed { idx, q ->
                val ans = answers.getOrElse(idx) { "" }.ifBlank { "Javob ko'rsatilmadi" }
                "${idx + 1}. $q -> $ans"
            }.joinToString("\n")

            val prompt = """
                Birlamchi shikoyat: "$complaint"
                
                Klinik savollarga bemor bergan javoblari:
                $qaList
                
                Ushbu ma'lumotlar asosida bemor uchun professional, tushunarli va to'liq tibbiy xulosa bering ($lang tilida):
                
                1. 🔍 **Ehtimoliy tashxis va sabablar**:
                   - Nima sababdan bu holat yuzaga kelgan bo'lishi mumkin?
                2. 🩺 **Tavsiya etiladigan mutaxassis shifokor**:
                   - Qaysi ixtisoslikdagi shifokor ko'rigiga borish kerak?
                3. 🧪 **Tavsiya etiladigan tahlillar**:
                   - Qanday laboratoriya yoki UTT/MRT tekshiruvlari foydali?
                4. 💡 **Uy sharoitidagi xavfsiz tavsiyalar**:
                   - Shifokor qabuligacha nimalar qilish mumkin?
                5. 🚨 **Favqulodda belgilar (Qizil bayroqlar)**:
                   - Qanday holatda zudlik bilan 103 ga qo'ng'iroq qilish kerak?
            """.trimIndent()

            val response = GeminiClient.generateText(prompt, "Siz bosh shifokor va mohir klinik diagnostiksiz.")
            _symptomDynamicAnalysisResult.value = response
            _isAnalyzingSymptomAnswers.value = false

            val user = currentUser.value
            if (user != null) {
                dao.insertSymptomCheck(
                    SymptomCheck(
                        userId = user.uid,
                        symptomsInput = "$complaint (Savol-javobli tahlil)",
                        bodyPart = "General",
                        resultJson = response
                    )
                )
            }
        }
    }

    fun addSimpleReminder(medName: String, hhmm: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val r = ReminderLocal(
                userId = user.uid,
                medicineName = medName,
                time = hhmm,
                frequency = "Har kuni",
                type = "medicine",
                targetFamilyMember = null,
                notes = "MedAI Yordamchi orqali qo'shildi"
            )
            dao.insertReminder(r)
            Toast.makeText(getApplication(), "Eslatma saqlandi: $medName ($hhmm)", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Lab Report Vision Analysis (Premium) ---
    fun analyzeLabReportImage(imageBase64: String, customPrompt: String? = null) {
        viewModelScope.launch {
            _isLoadingLab.value = true
            _labAnalysisResult.value = ""
            val lang = _currentLanguage.value

            val prompt = customPrompt ?: """
                Analyze this medical lab result image. Identify all key metrics (e.g. Hemoglobin, Glucose, Cholesterol, WBC), identify normal reference ranges, highlight any abnormal (high/low) values. Explain the clinical significance in simple $lang language.
                Generate a structured text table.
            """.trimIndent()

            // Call Multimodal Gemini Vision
            val response = GeminiClient.generateMultimodal(prompt, imageBase64, "image/jpeg")
            _labAnalysisResult.value = response
            _isLoadingLab.value = false

            // Save in history
            val user = currentUser.value
            if (user != null) {
                dao.insertLabResult(
                    LabResultLocal(
                        userId = user.uid,
                        imagePath = "simulated_storage_path",
                        analysisText = response,
                        valuesJson = "[]"
                    )
                )
            }
        }
    }

    fun deleteLabResult(id: Int) {
        viewModelScope.launch {
            dao.deleteLabResult(id)
        }
    }

    fun clearLabAnalysisResult() {
        _labAnalysisResult.value = ""
    }

    // --- Family & Immunization Operations ---
    fun linkFamilyMemberSubAccount(
        name: String,
        relation: String,
        email: String,
        phone: String
    ) {
        viewModelScope.launch {
            val randomId = "family_uid_" + java.util.UUID.randomUUID().toString().take(6)
            val newMember = FamilyMemberLocal(
                uid = randomId,
                name = name,
                email = email,
                phone = phone,
                relation = relation,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                healthScore = 100,
                stepsToday = 0,
                lastActive = System.currentTimeMillis(),
                activeRemindersCount = 0,
                sosStatus = false,
                inviteStatus = "accepted", // immediately linked sub-account!
                isInvitedByMe = true
            )
            dao.insertFamilyMember(newMember)
            
            // Seed a standard immunization schedule for children/adults based on relation
            seedStandardImmunizations(randomId, relation)
        }
    }

    fun seedStandardImmunizations(familyMemberUid: String, relation: String) {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            
            val isChild = relation.lowercase() in listOf("child", "qizi", "o'g'li", "syn", "doch")
            val defaultVaccines = if (isChild) {
                listOf(
                    Triple("BCG (Silga qarshi / Против туберкулеза)", "Tuberculosis", "At birth"),
                    Triple("Hepatitis B (Gepatit B / Гепатит B)", "Hepatitis B", "At birth"),
                    Triple("OPV 1 (Sholga qarshi / Против полиомиелита)", "Poliomyelitis", "2 months"),
                    Triple("Pentavalent 1 (Besh valentli / Пентавалент)", "Diphtheria, Tetanus, Pertussis, HepB, Hib", "2 months"),
                    Triple("Rota 1 (Rotavirusga qarshi / Ротавирус)", "Rotavirus diarrhea", "2 months"),
                    Triple("Pentavalent 2 (Besh valentli / Пентавалент)", "Diphtheria, Tetanus, Pertussis, HepB, Hib", "3 months"),
                    Triple("OPV 2 (Sholga qarshi / Против полиомиелита)", "Poliomyelitis", "3 months"),
                    Triple("Pentavalent 3 (Besh valentli / Пентавалент)", "Diphtheria, Tetanus, Pertussis, HepB, Hib", "4 months"),
                    Triple("OPV 3 (Sholga qarshi / Против полиомиелита)", "Poliomyelitis", "4 months"),
                    Triple("MMR 1 (Qizamiq, Parotit, Qizilcha / КПК)", "Measles, Mumps, Rubella", "12 months")
                )
            } else {
                listOf(
                    Triple("Flu Vaccine (Grippga qarshi / Грипп)", "Influenza", "Yearly"),
                    Triple("Tdap Booster (Koksarsh / АДС-М)", "Tetanus, Diphtheria, Pertussis", "Every 10 years"),
                    Triple("Hepatitis B (Gepatit B adult / Гепатит B взрослым)", "Hepatitis B", "As recommended"),
                    Triple("HPV Vaccine (Odam papillomasi / ВПЧ)", "Cervical cancer prevention", "Under age 26")
                )
            }

            defaultVaccines.forEachIndexed { index, v ->
                val dueDays = when (v.third) {
                    "At birth" -> 7
                    "2 months" -> 60
                    "3 months" -> 90
                    "4 months" -> 120
                    "12 months" -> 365
                    "Yearly" -> 180
                    else -> 120
                }
                val dueDateStr = format.format(Date(today + dueDays * 24L * 3600L * 1000L))
                
                dao.insertImmunizationRecord(
                    ImmunizationRecordLocal(
                        familyMemberUid = familyMemberUid,
                        vaccineName = v.first,
                        targetDisease = v.second,
                        scheduledAge = v.third,
                        dueDate = dueDateStr,
                        status = "Pending",
                        completedDate = null,
                        administeredBy = null,
                        notes = "Oila a'zosi yaratilganda avtomatik ravishda tayinlangan."
                    )
                )
            }
        }
    }

    fun addCustomImmunizationRecord(
        familyMemberUid: String,
        vaccineName: String,
        targetDisease: String,
        scheduledAge: String,
        dueDate: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            dao.insertImmunizationRecord(
                ImmunizationRecordLocal(
                    familyMemberUid = familyMemberUid,
                    vaccineName = vaccineName,
                    targetDisease = targetDisease,
                    scheduledAge = scheduledAge,
                    dueDate = dueDate,
                    status = "Pending",
                    notes = notes
                )
            )
        }
    }

    fun updateImmunizationRecord(record: ImmunizationRecordLocal) {
        viewModelScope.launch {
            dao.insertImmunizationRecord(record)
        }
    }

    fun deleteImmunizationRecord(id: Int) {
        viewModelScope.launch {
            dao.deleteImmunizationRecord(id)
        }
    }

    fun removeFamilyMember(uid: String) {
        viewModelScope.launch {
            dao.deleteFamilyMember(uid)
        }
    }

    // --- Reminders System ---
    fun addReminder(medicineName: String, time: String, frequency: String, type: String, targetFamily: String?, notes: String?) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val r = ReminderLocal(
                userId = user.uid,
                medicineName = medicineName,
                time = time,
                frequency = frequency,
                type = type,
                targetFamilyMember = targetFamily,
                notes = notes
            )
            dao.insertReminder(r)
            Toast.makeText(getApplication(), "Eslatma muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            dao.deleteReminder(id)
        }
    }

    fun toggleReminderActive(reminder: ReminderLocal) {
        viewModelScope.launch {
            dao.insertReminder(reminder.copy(isActive = !reminder.isActive))
        }
    }

    // --- Emergency SOS System ---
    fun triggerSOS() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val loc = gpsLocation.value

            dao.insertNotification(NotificationLocal(
                userId = user.uid,
                title = "🆘 SOS signali yuborildi",
                message = "Joylashuvingiz: $loc. Oila a'zolaringizga xabar yuborildi.",
                type = "sos"
            ))
            Toast.makeText(getApplication(), "SOS signali faollashtirildi! Favqulodda yordam jo'natilmoqda.", Toast.LENGTH_LONG).show()

            // Alert accepted family members as per specifications
            sendSimulatedPush(
                "🆘 ${user.name} SOS signal yubordi!",
                "Unga yordam kerak! Joylashuv: https://maps.google.com/?q=$loc",
                "sos"
            )
        }
    }

    // --- In-App & Simulated Push Notification System ---
    fun sendSimulatedPush(title: String, message: String, type: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                dao.insertNotification(
                    NotificationLocal(
                        userId = user.uid,
                        title = title,
                        message = message,
                        type = type
                    )
                )
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            dao.markAllNotificationsAsRead()
        }
    }

    // --- Premium Flow & Payment Request ---
    fun submitPaymentCheck(imageBase64: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val rId = UUID.randomUUID().toString()
            val request = PaymentRequestLocal(
                id = rId,
                userId = user.uid,
                userName = user.name,
                userEmail = user.email,
                userPhone = user.phone,
                checkImageUrl = "data:image/jpeg;base64,$imageBase64",
                status = "pending",
                rejectionReason = "",
                submittedAt = System.currentTimeMillis(),
                reviewedAt = null,
                reviewedBy = null
            )
            dao.insertPaymentRequest(request)

            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "userId" to request.userId,
                    "userName" to request.userName,
                    "userEmail" to request.userEmail,
                    "userPhone" to request.userPhone,
                    "checkImageUrl" to request.checkImageUrl,
                    "status" to request.status,
                    "rejectionReason" to request.rejectionReason,
                    "submittedAt" to request.submittedAt,
                    "reviewedAt" to request.reviewedAt,
                    "reviewedBy" to request.reviewedBy
                )
                db.collection("paymentRequests").document(rId).set(data)
            } catch (ex: Exception) {
                Log.e("FirestorePayment", "Failed to upload: ${ex.message}")
            }

            Toast.makeText(getApplication(), "Chek muvaffaqiyatli yuborildi! Admin tez orada tasdiqlaydi.", Toast.LENGTH_LONG).show()
        }
    }

    // --- Admin panel actions ---
    fun approvePayment(request: PaymentRequestLocal) {
        viewModelScope.launch {
            val updated = request.copy(
                status = "approved",
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = currentAdminEmail()
            )
            dao.insertPaymentRequest(updated)
            logAdminAction("Approve Payment", request.userId, "To'lov so'rovi tasdiqlandi: ${request.id}")

            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("paymentRequests").document(request.id).update(
                    "status", "approved",
                    "reviewedAt", updated.reviewedAt,
                    "reviewedBy", updated.reviewedBy
                )
            } catch (ex: Exception) {
                Log.e("FirestorePayment", "Failed to approve: ${ex.message}")
            }

            // Trigger Premium Grant
            val user = dao.getCurrentUser()
            if (user != null && user.uid == request.userId) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 30)
                dao.insertUser(user.copy(isPremium = true, premiumExpiry = cal.timeInMillis))
                
                // Alert user
                dao.insertNotification(NotificationLocal(
                    userId = user.uid,
                    title = "🎉 Premium faollashtirildi!",
                    message = "To'lov tasdiqlandi. Barcha premium imkoniyatlardan cheksiz foydalanishingiz mumkin!",
                    type = "premium"
                ))
            }
        }
    }

    fun rejectPayment(request: PaymentRequestLocal, reason: String) {
        viewModelScope.launch {
            val updated = request.copy(
                status = "rejected",
                rejectionReason = reason,
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = currentAdminEmail()
            )
            dao.insertPaymentRequest(updated)
            logAdminAction("Reject Payment", request.userId, "To'lov so'rovi rad etildi: ${request.id}. Sabab: $reason")

            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("paymentRequests").document(request.id).update(
                    "status", "rejected",
                    "rejectionReason", reason,
                    "reviewedAt", updated.reviewedAt,
                    "reviewedBy", updated.reviewedBy
                )
            } catch (ex: Exception) {
                Log.e("FirestorePayment", "Failed to reject: ${ex.message}")
            }

            // Alert user
            dao.insertNotification(NotificationLocal(
                userId = request.userId,
                title = "❌ To'lov rad etildi",
                message = "Rad etilish sababi: $reason. Iltimos, qaytadan urinib ko'ring.",
                type = "premium"
            ))
        }
    }

    fun toggleMaintenanceMode(enabled: Boolean) {
        viewModelScope.launch {
            val config = dao.getAppConfig() ?: AppConfigLocal()
            dao.insertAppConfig(config.copy(maintenanceMode = enabled))
        }
    }

    fun banUserToggle(uid: String) {
        viewModelScope.launch {
            val user = dao.getCurrentUser()
            if (user != null && user.uid == uid) {
                dao.insertUser(user.copy(isBanned = !user.isBanned))
            }
        }
    }

    // --- Mock Data Prep ---
    private suspend fun addMockFamilyData(currentUserId: String) {
        // Mock family members
        val member1 = FamilyMemberLocal(
            uid = "family_uid_1",
            name = "Malika Karimova",
            email = "malika@gmail.com",
            phone = "+998 91 222 33 44",
            relation = "Turmush o'rtog'i",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
            healthScore = 0,
            stepsToday = 0,
            lastActive = System.currentTimeMillis(),
            activeRemindersCount = 0,
            sosStatus = false,
            inviteStatus = "accepted",
            isInvitedByMe = true
        )
        val member2 = FamilyMemberLocal(
            uid = "family_uid_2",
            name = "Jasur Karimov",
            email = "jasur@gmail.com",
            phone = "+998 90 777 88 99",
            relation = "O'g'li",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
            healthScore = 0,
            stepsToday = 0,
            lastActive = System.currentTimeMillis(),
            activeRemindersCount = 0,
            sosStatus = false,
            inviteStatus = "accepted",
            isInvitedByMe = true
        )
        val invite1 = FamilyMemberLocal(
            uid = "family_uid_3",
            name = "Soliha Karimova",
            email = "soliha@gmail.com",
            phone = "+998 93 456 12 34",
            relation = "Qizi",
            avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
            healthScore = 0,
            stepsToday = 0,
            lastActive = System.currentTimeMillis(),
            activeRemindersCount = 0,
            sosStatus = false,
            inviteStatus = "pending",
            isInvitedByMe = false // they invited us!
        )

        dao.insertFamilyMember(member1)
        dao.insertFamilyMember(member2)
        dao.insertFamilyMember(invite1)
        seedStandardImmunizations(member1.uid, member1.relation)
        seedStandardImmunizations(member2.uid, member2.relation)
    }

    // --- Daily Health Tracking & Analytics ---
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun getOrCreateTodayMetrics(): DailyHealthMetricsLocal {
        val date = getTodayDateString()
        val user = currentUser.value
        val userId = user?.uid ?: "default_user"
        val existing = dao.getDailyMetrics(date)
        if (existing != null) return existing
        
        val newMetrics = DailyHealthMetricsLocal(
            date = date,
            userId = userId,
            waterGoal = 8
        )
        dao.insertDailyMetrics(newMetrics)
        return newMetrics
    }

    fun updateWaterProgress(glassesDelta: Int) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            val newGlasses = (metrics.waterGlasses + glassesDelta).coerceAtLeast(0)
            dao.insertDailyMetrics(metrics.copy(waterGlasses = newGlasses))
            
            // Check water achievement: "Suv ichuvchi 💧" — 7 days water goal met
            checkWaterAchievement()
            
            // Recalculate health score
            recalculateHealthScore()
        }
    }

    fun updateWaterGoal(goal: Int) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            dao.insertDailyMetrics(metrics.copy(waterGoal = goal))
        }
    }

    fun logWeightMetrics(weight: Double) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            dao.insertDailyMetrics(metrics.copy(weight = weight))
            
            // Also update current user's weight
            val user = currentUser.value
            if (user != null) {
                dao.insertUser(user.copy(weight = weight))
            }
            
            recalculateHealthScore()
            logVitalToFirestore(weight = weight)
        }
    }

    fun logBloodPressureMetrics(systolic: Int, diastolic: Int) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            dao.insertDailyMetrics(metrics.copy(bpSystolic = systolic, bpDiastolic = diastolic))
            
            recalculateHealthScore()
            logVitalToFirestore(bpSystolic = systolic, bpDiastolic = diastolic)
        }
    }

    fun logHeartRateMetrics(bpm: Int) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            dao.insertDailyMetrics(metrics.copy(heartRate = bpm))
            
            // Alert if BPM < 50 or > 120
            if (bpm < 50 || bpm > 120) {
                sendSimulatedPush(
                    "⚠️ Puls ogohlantirishi!",
                    "Yurak urish tezligi normal emas: $bpm BPM. Iltimos, dam oling yoki shifokorga murojaat qiling.",
                    "system"
                )
            }
            
            recalculateHealthScore()
            logVitalToFirestore(heartRate = bpm)
        }
    }

    fun logSleepMetrics(hours: Double, bedtime: String, waketime: String) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            dao.insertDailyMetrics(metrics.copy(sleepHours = hours, sleepBedtime = bedtime, sleepWaketime = waketime))
            
            recalculateHealthScore()
        }
    }

    fun logMealMetrics(title: String, calories: Int, type: String) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            val mealsArray = try { org.json.JSONArray(metrics.mealsJson) } catch(e: Exception) { org.json.JSONArray() }
            val mealObj = org.json.JSONObject().apply {
                put("title", title)
                put("calories", calories)
                put("type", type)
                put("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
            }
            mealsArray.put(mealObj)
            
            dao.insertDailyMetrics(metrics.copy(
                mealsJson = mealsArray.toString()
            ))
            
            recalculateHealthScore()
        }
    }

    fun completeReminder(reminderId: Int) {
        viewModelScope.launch {
            val metrics = getOrCreateTodayMetrics()
            val completedList = try { org.json.JSONArray(metrics.completedRemindersJson) } catch(e: Exception) { org.json.JSONArray() }
            
            // Check if already completed
            var exists = false
            for (i in 0 until completedList.length()) {
                if (completedList.getInt(i) == reminderId) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                completedList.put(reminderId)
                dao.insertDailyMetrics(metrics.copy(completedRemindersJson = completedList.toString()))
                
                // Increment achievements count
                checkCompletedReminderAchievements()
            }
            
            recalculateHealthScore()
        }
    }

    // --- Streak & Achievements ---
    fun unlockAchievement(id: String, nameUz: String, nameRu: String, nameEn: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val arr = try { org.json.JSONArray(user.unlockedAchievementsJson) } catch(e: Exception) { org.json.JSONArray() }
            
            // Check if already unlocked
            var unlocked = false
            for (i in 0 until arr.length()) {
                if (arr.getString(i) == id) {
                    unlocked = true
                    break
                }
            }
            if (!unlocked) {
                arr.put(id)
                dao.insertUser(user.copy(unlockedAchievementsJson = arr.toString()))
                
                // Show celebration toast / push notification
                val lang = _currentLanguage.value
                val title = when (lang) {
                    "uz" -> "Yangi yutuq ochildi! 🏆"
                    "ru" -> "Новое достижение разблокировано! 🏆"
                    else -> "New Achievement Unlocked! 🏆"
                }
                val msg = when (lang) {
                    "uz" -> "\"$nameUz\" yutug'ini qo'lga kiritdingiz!"
                    "ru" -> "Вы получили достижение \"$nameRu\"!"
                    else -> "You unlocked the \"$nameEn\" achievement!"
                }
                sendSimulatedPush(title, msg, "system")
            }
        }
    }

    private fun checkCompletedReminderAchievements() {
        viewModelScope.launch {
            val metricsList = dao.getAllDailyMetricsFlow().first()
            
            // 1. Beginner Achievement (1st reminder done)
            val totalDone = metricsList.sumOf { 
                try { org.json.JSONArray(it.completedRemindersJson).length() } catch(e: Exception) { 0 }
            } + firestoreReminders.value.sumOf { it.completedDates.size }
            if (totalDone >= 1) {
                unlockAchievement("beginner", "Boshlang'ich 🌱", "Новичок 🌱", "Beginner 🌱")
            }
            
            // 2. Regular Achievement (50 reminders done)
            if (totalDone >= 50) {
                unlockAchievement("regular", "Muntazam 💊", "Регулярный 💊", "Regular 💊")
            }
            
            // 3. Streak Achievements
            val activeRemindersCount = reminders.value.count { it.isActive } + firestoreReminders.value.count { it.isActive }
            if (activeRemindersCount > 0) {
                var streak = 0
                for (m in metricsList) {
                    val completed = try { org.json.JSONArray(m.completedRemindersJson).length() } catch(e: Exception) { 0 }
                    if (completed >= activeRemindersCount) {
                        streak++
                    } else {
                        break
                    }
                }
                if (streak >= 7) {
                    unlockAchievement("week", "Bir hafta 🏅", "Одна неделя 🏅", "One Week 🏅")
                }
                if (streak >= 30) {
                    unlockAchievement("month", "Bir oy 🏆", "Один месяц 🏆", "One Month 🏆")
                }
            }
        }
    }

    private fun checkWaterAchievement() {
        viewModelScope.launch {
            val metricsList = dao.getAllDailyMetricsFlow().first()
            val metDays = metricsList.count { it.waterGlasses >= it.waterGoal }
            if (metDays >= 7) {
                unlockAchievement("water", "Suv ichuvchi 💧", "Любитель воды 💧", "Water Drinker 💧")
            }
        }
    }

    fun recalculateHealthScore() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = dao.getCurrentUser() ?: return@launch
            val today = getTodayDateString()
            val metrics = dao.getDailyMetrics(today)
            
            // Starts strictly at 0 for all new and inactive users
            var score = 0
            
            if (metrics != null) {
                // 1. Dori qabul qilish qoidalariga amal qilish (Medication adherence): max +25
                val completedRemindersCount = try { org.json.JSONArray(metrics.completedRemindersJson).length() } catch(e: Exception) { 0 }
                val completedFirestoreCount = firestoreReminders.value.count { it.completedDates.contains(today) }
                val totalRemindersDone = completedRemindersCount + completedFirestoreCount
                score += (totalRemindersDone * 10).coerceAtMost(25)
                
                // 2. Arterial qon bosimi nazorati (Blood Pressure): max +15
                if (metrics.bpSystolic > 0 && metrics.bpDiastolic > 0) {
                    score += 10
                    // Kasallik qoidasiga amal qilib normada ushlash
                    if (metrics.bpSystolic in 90..130 && metrics.bpDiastolic in 60..85) {
                        score += 5
                    }
                }
                
                // 3. Yurak urishi / Puls nazorati (Heart Rate): max +10
                if (metrics.heartRate > 0) {
                    score += 5
                    if (metrics.heartRate in 55..100) {
                        score += 5
                    }
                }
                
                // 4. Tana vazni / BMI nazorati (Weight): max +5
                if (metrics.weight > 0.0) {
                    score += 5
                }
                
                // 5. Uyqu gigiyenasi va rejimi (Sleep): max +10
                if (metrics.sleepHours > 0.0) {
                    score += 5
                    if (metrics.sleepHours in 7.0..9.0) {
                        score += 5
                    }
                }
                
                // 6. Suv balansi (Hydration): max +15
                if (metrics.waterGlasses > 0) {
                    score += metrics.waterGlasses.coerceAtMost(10)
                    if (metrics.waterGlasses >= metrics.waterGoal) {
                        score += 5
                    }
                }
                
                // 7. Jismoniy faollik (Steps): max +15
                val stepsCount = if (metrics.steps > 0) metrics.steps else _dailySteps.value
                when {
                    stepsCount >= 8000 -> score += 15
                    stepsCount >= 5000 -> score += 10
                    stepsCount >= 1000 -> score += 5
                }
                
                // 8. Sog'lom ovqatlanish rejimi (Meals): max +10
                val mealsCount = try { org.json.JSONArray(metrics.mealsJson).length() } catch(e: Exception) { 0 }
                score += (mealsCount * 5).coerceAtMost(10)
                
                // 9. Kasallik simptomlarini tekshirish / Shifokor tahlili: +10
                if (metrics.symptomCheckCompleted || symptomChecks.value.isNotEmpty()) {
                    score += 10
                }
            }
            
            val finalScore = score.coerceIn(0, 100)
            if (finalScore != user.healthScore) {
                dao.insertUser(user.copy(healthScore = finalScore))
            }
        }
    }

    /**
     * Barcha statistikani va ballarni 0 ga tushirish (Reset all statistics to 0)
     */
    fun resetAllStatistics(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _dailySteps.value = 0
            _loggedWeight.value = 0.0
            _loggedSleep.value = 0.0
            _loggedWater.value = 0
            val today = getTodayDateString()
            val user = currentUser.value
            if (user != null) {
                dao.insertDailyMetrics(
                    DailyHealthMetricsLocal(
                        date = today,
                        userId = user.uid,
                        steps = 0,
                        waterGlasses = 0,
                        waterGoal = 8,
                        weight = 0.0,
                        bpSystolic = 0,
                        bpDiastolic = 0,
                        heartRate = 0,
                        sleepHours = 0.0,
                        mealsJson = "[]",
                        completedRemindersJson = "[]",
                        symptomCheckCompleted = false
                    )
                )
                dao.insertUser(user.copy(healthScore = 0))
            }
            logAdminAction("RESET_STATISTICS", user?.email ?: "local", "Barcha statistika va ko'rsatkichlar 0 ga tushirildi")
            onComplete()
        }
    }

    // --- Allergy Management ---
    fun addAllergy(name: String, type: String) {
        viewModelScope.launch {
            val user = dao.getCurrentUser() ?: return@launch
            val arr = try { org.json.JSONArray(user.allergiesJson) } catch(e: Exception) { org.json.JSONArray() }
            
            // Check if already exists
            var exists = false
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("name").equals(name, ignoreCase = true)) {
                    exists = true
                    break
                }
            }
            if (!exists) {
                val allergyObj = org.json.JSONObject().apply {
                    put("name", name)
                    put("type", type)
                    put("addedAt", System.currentTimeMillis())
                }
                arr.put(allergyObj)
                dao.insertUser(user.copy(allergiesJson = arr.toString()))
                Toast.makeText(getApplication(), "Allergiya muvaffaqiyatli qo'shildi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteAllergy(name: String) {
        viewModelScope.launch {
            val user = dao.getCurrentUser() ?: return@launch
            val arr = try { org.json.JSONArray(user.allergiesJson) } catch(e: Exception) { org.json.JSONArray() }
            val newArr = org.json.JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (!obj.getString("name").equals(name, ignoreCase = true)) {
                    newArr.put(obj)
                }
            }
            dao.insertUser(user.copy(allergiesJson = newArr.toString()))
            Toast.makeText(getApplication(), "Allergiya o'chirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkMedicinesForAllergies(aiResultText: String): List<String> {
        val user = currentUser.value ?: return emptyList()
        val allergiesList = mutableListOf<String>()
        try {
            val arr = org.json.JSONArray(user.allergiesJson)
            for (i in 0 until arr.length()) {
                val name = arr.getJSONObject(i).getString("name")
                if (aiResultText.contains(name, ignoreCase = true)) {
                    allergiesList.add(name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return allergiesList
    }

    // --- Drug Interaction Checker ---
    fun checkDrugInteractions(drugs: List<String>) {
        viewModelScope.launch {
            _isCheckingInteractions.value = true
            _interactionResult.value = ""
            val lang = _currentLanguage.value
            val listStr = drugs.filter { it.isNotBlank() }.joinToString(", ")
            
            val prompt = """
                Check medical interactions between these drugs: $listStr.
                Are they safe together? What are the risks and warnings?
                
                You MUST start your response with exactly one of these status markers based on clinical risk:
                - "STATUS: SAFE" (if safe to take together)
                - "STATUS: CAUTION" (if they require cautious use or monitoring)
                - "STATUS: DANGEROUS" (if contraindicated or highly risky together)
                
                Then provide a clear, understandable clinical explanation in $lang language.
            """.trimIndent()
            
            val response = GeminiClient.generateText(prompt, "You are an expert clinical pharmacologist.")
            _interactionResult.value = response
            _isCheckingInteractions.value = false
        }
    }

    // --- Prescription Scanner ---
    fun scanPrescriptionImage(imageBase64: String) {
        viewModelScope.launch {
            _isScanningPrescription.value = true
            _prescriptionScanResult.value = ""
            val lang = _currentLanguage.value
            val user = currentUser.value ?: return@launch
            
            val prompt = """
                This is a medical prescription. List all medicines, dosage, frequency, duration in $lang language.
                Explain each medicine simply. Return the result in a clean markdown format.
            """.trimIndent()
            
            val response = GeminiClient.generateMultimodal(prompt, imageBase64, "image/jpeg")
            _prescriptionScanResult.value = response
            _isScanningPrescription.value = false
            
            // Save to database
            dao.insertPrescriptionScan(
                PrescriptionScanLocal(
                    userId = user.uid,
                    imagePath = "simulated_prescription_path",
                    rawResult = response
                )
            )
        }
    }

    // --- Daily Nutrition AI Advisor ---
    fun analyzeNutritionDaily() {
        viewModelScope.launch {
            _isAnalyzingNutrition.value = true
            _nutritionAdvice.value = ""
            val lang = _currentLanguage.value
            val today = getTodayDateString()
            val metrics = dao.getDailyMetrics(today)
            val meals = metrics?.mealsJson ?: "[]"
            
            var caloriesSum = 0
            try {
                val arr = org.json.JSONArray(meals)
                for (i in 0 until arr.length()) {
                    caloriesSum += arr.getJSONObject(i).getInt("calories")
                }
            } catch(e: Exception) {}

            val prompt = """
                As an expert clinical nutritionist, analyze today's meal intake log for this user:
                Today's Meals: $meals
                Total Calories Consumed: $caloriesSum kcal (Daily goal: 2000 kcal).
                
                Provide personalized nutrition, metabolic, and portion size advice in $lang language. 
                Be encouraging, scientific, and actionable.
            """.trimIndent()
            
            val response = GeminiClient.generateText(prompt, "You are a professional clinical dietitian.")
            _nutritionAdvice.value = response
            _isAnalyzingNutrition.value = false
        }
    }

    // --- Online Appointment Booking ---
    fun submitAppointmentRequest(doctorId: String, doctorName: String, patientName: String, patientPhone: String, preferredTime: String) {
        viewModelScope.launch {
            dao.insertAppointmentRequest(
                AppointmentRequestLocal(
                    doctorId = doctorId,
                    doctorName = doctorName,
                    patientName = patientName,
                    patientPhone = patientPhone,
                    preferredTime = preferredTime,
                    status = "pending"
                )
            )
            Toast.makeText(getApplication(), "So'rov yuborildi! Admin tasdiqlashini kuting.", Toast.LENGTH_LONG).show()
        }
    }

    fun updateAppointmentStatus(id: Int, status: String, rejectionReason: String = "") {
        viewModelScope.launch {
            val list = appointmentRequests.value
            val req = list.find { it.id == id }
            if (req != null) {
                dao.insertAppointmentRequest(req.copy(status = status, rejectionReason = rejectionReason))
                
                // Add system notification for user
                val title = if (status == "confirmed") "✅ Qabul tasdiqlandi!" else "❌ Qabul rad etildi"
                val msg = if (status == "confirmed") {
                    "Doktor ${req.doctorName} bilan ${req.preferredTime} dagi uchrashuv tasdiqlandi."
                } else {
                    "Doktor ${req.doctorName} uchrashuvni rad etdi. Sababi: $rejectionReason"
                }
                sendSimulatedPush(title, msg, "system")
            }
        }
    }

    // --- Medical Documents Management ---
    fun addMedicalDocument(title: String, docType: String, imageBase64: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            dao.insertMedicalDocument(
                MedicalDocumentLocal(
                    userId = user.uid,
                    title = title,
                    docType = docType,
                    imagePath = "data:image/jpeg;base64,$imageBase64"
                )
            )
            Toast.makeText(getApplication(), "Hujjat muvaffaqiyatli yuklandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteMedicalDocument(id: Int) {
        viewModelScope.launch {
            dao.deleteMedicalDocument(id)
        }
    }

    // --- Admin Panel - Health Tips Management ---
    fun addHealthTip(uz: String, ru: String, en: String, orderIndex: Int) {
        viewModelScope.launch {
            dao.insertHealthTip(HealthTipLocal(uz = uz, ru = ru, en = en, orderIndex = orderIndex))
            Toast.makeText(getApplication(), "Maslahat muvaffaqiyatli qo'shildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteHealthTip(id: Int) {
        viewModelScope.launch {
            dao.deleteHealthTip(id)
        }
    }

    fun getTodayHealthTip(): HealthTipLocal {
        val tips = healthTips.value
        if (tips.isEmpty()) {
            return HealthTipLocal(
                uz = "Har kuni 8 stakan suv iching 💧",
                ru = "Пейте 8 стаканов воды каждый день 💧",
                en = "Drink 8 glasses of water every day 💧"
            )
        }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return tips[dayOfYear % tips.size]
    }

    // Resolves the acting admin's email dynamically (Firebase Auth first, then the local
    // profile) instead of hardcoding it, so audit trails reflect who actually did the action.
    private fun currentAdminEmail(): String {
        return try {
            val fbAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            fbAuth.currentUser?.email ?: currentUser.value?.email ?: SUPER_ADMIN_EMAIL
        } catch (e: Exception) {
            currentUser.value?.email ?: SUPER_ADMIN_EMAIL
        }
    }

    fun logAdminAction(action: String, targetUser: String, details: String) {
        viewModelScope.launch {
            val adminEmail = currentAdminEmail()

            val newLog = AdminLog(
                adminEmail = adminEmail,
                action = action,
                targetUser = targetUser,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            dao.insertAdminLog(newLog)

            // Dynamic Firebase Firestore track
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val logMap = hashMapOf(
                    "adminEmail" to newLog.adminEmail,
                    "action" to newLog.action,
                    "targetUser" to newLog.targetUser,
                    "details" to newLog.details,
                    "timestamp" to newLog.timestamp
                )
                db.collection("adminLogs")
                    .add(logMap)
                    .addOnSuccessListener {
                        Log.d("FirestoreLog", "Admin log successfully uploaded to Firestore!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreLog", "Failed to upload log to Firestore: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("FirestoreLog", "Firestore upload bypassed/failed: ${e.message}")
            }
        }
    }

    fun addErrorLog(screen: String, errorMessage: String) {
        viewModelScope.launch {
            val user = currentUser.value
            dao.insertErrorLog(ErrorLog(
                userId = user?.uid ?: "anonymous",
                screen = screen,
                errorMessage = errorMessage,
                appVersion = "1.0",
                deviceInfo = "Android Emulator / Local Device"
            ))
        }
    }

    fun resolveErrorLog(id: Int) {
        viewModelScope.launch {
            dao.updateErrorLogResolved(id, true)
            logAdminAction("Resolve Error Log", "System", "Error log ID #$id resolved.")
        }
    }

    fun updatePopupBanner(active: Boolean, imageUrl: String, titleUz: String, titleRu: String, titleEn: String, subtitleUz: String, subtitleRu: String, subtitleEn: String, btnUz: String, btnRu: String, btnEn: String, actionType: String, actionVal: String, start: Long, end: Long) {
        viewModelScope.launch {
            val banner = PopupBannerConfig(
                id = "global_banner",
                active = active,
                imageUrl = imageUrl,
                titleUz = titleUz,
                titleRu = titleRu,
                titleEn = titleEn,
                subtitleUz = subtitleUz,
                subtitleRu = subtitleRu,
                subtitleEn = subtitleEn,
                buttonTextUz = btnUz,
                buttonTextRu = btnRu,
                buttonTextEn = btnEn,
                actionType = actionType,
                actionValue = actionVal,
                startDate = start,
                endDate = end
            )
            dao.insertPopupBannerConfig(banner)
            logAdminAction("Update Popup Banner", "All Users", "Banner active=$active, title=$titleUz")
            Toast.makeText(getApplication(), "Pop-up o'zgarishlari saqlandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateAnnouncement(active: Boolean, textUz: String, textRu: String, textEn: String, color: String) {
        viewModelScope.launch {
            val config = AnnouncementConfig(
                id = "global_announcement",
                active = active,
                textUz = textUz,
                textRu = textRu,
                textEn = textEn,
                color = color
            )
            dao.insertAnnouncementConfig(config)
            logAdminAction("Update Announcement", "All Users", "Announcement active=$active, text=$textUz")
            Toast.makeText(getApplication(), "E'lon o'zgarishlari saqlandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateAppVersion(force: Boolean, minVer: String, msgUz: String, msgRu: String, msgEn: String, storeUrl: String) {
        viewModelScope.launch {
            val config = AppVersionConfig(
                id = "global_version",
                forceUpdate = force,
                minVersion = minVer,
                messageUz = msgUz,
                messageRu = msgRu,
                messageEn = msgEn,
                storeUrl = storeUrl
            )
            dao.insertAppVersionConfig(config)
            logAdminAction("Update App Version", "All Users", "Min Version=$minVer, Force=$force")
            Toast.makeText(getApplication(), "Ilova versiyasi parametrlari saqlandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateFeatureFlags(symptom: Boolean, doctor: Boolean, lab: Boolean, family: Boolean, sos: Boolean, analytics: Boolean) {
        viewModelScope.launch {
            val config = FeatureFlagsConfig(
                id = "global_flags",
                symptomChecker = symptom,
                aiDoctor = doctor,
                labAnalysis = lab,
                family = family,
                sos = sos,
                analytics = analytics
            )
            dao.insertFeatureFlagsConfig(config)
            logAdminAction("Update Feature Flags", "System", "Flags updated: Symptom=$symptom, Doctor=$doctor, Lab=$lab, Family=$family, SOS=$sos, Analytics=$analytics")
            Toast.makeText(getApplication(), "Funksiyalar boshqaruvi saqlandi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun addDisease(nameUz: String, nameRu: String, nameEn: String, symptomsUz: String, descUz: String, descRu: String, descEn: String, specType: String, severity: String) {
        viewModelScope.launch {
            dao.insertDisease(DiseaseEntry(
                nameUz = nameUz,
                nameRu = nameRu,
                nameEn = nameEn,
                symptomsUz = symptomsUz,
                descriptionUz = descUz,
                descriptionRu = descRu,
                descriptionEn = descEn,
                specialistType = specType,
                severity = severity
            ))
            logAdminAction("Add Disease", nameUz, "Kasallik bazaga qo'shildi: $nameUz")
            Toast.makeText(getApplication(), "Kasallik qo'shildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDisease(id: Int) {
        viewModelScope.launch {
            dao.deleteDisease(id)
            logAdminAction("Delete Disease", "ID #$id", "Kasallik bazadan o'chirildi.")
            Toast.makeText(getApplication(), "O'chirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun addMedicine(name: String, description: String, dosage: String, sideEffects: String, category: String) {
        viewModelScope.launch {
            dao.insertMedicine(MedicineEntry(
                name = name,
                description = description,
                dosage = dosage,
                sideEffects = sideEffects,
                category = category
            ))
            logAdminAction("Add Medicine", name, "Dori bazaga qo'shildi: $name")
            Toast.makeText(getApplication(), "Dori qo'shildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteMedicine(id: Int) {
        viewModelScope.launch {
            dao.deleteMedicine(id)
            logAdminAction("Delete Medicine", "ID #$id", "Dori bazadan o'chirildi.")
            Toast.makeText(getApplication(), "O'chirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun scheduleNotification(title: String, message: String, time: Long, repeat: String, target: String, value: String) {
        viewModelScope.launch {
            dao.insertScheduledNotification(ScheduledNotification(
                title = title,
                message = message,
                scheduledTime = time,
                repeatType = repeat,
                targetType = target,
                targetValue = value
            ))
            logAdminAction("Schedule Notification", target, "Scheduled announcement for $repeat. Title: $title")
            Toast.makeText(getApplication(), "Xabarnoma rejalashtirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteScheduledNotification(id: Int) {
        viewModelScope.launch {
            dao.deleteScheduledNotification(id)
            logAdminAction("Delete Scheduled Notif", "ID #$id", "Scheduled notification removed.")
            Toast.makeText(getApplication(), "O'chirildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendNotificationToColdUsers() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val users = dao.getAllSystemUsers()
            var count = 0
            users.forEach { user ->
                val diffDays = (now - user.lastActive) / (24 * 3600 * 1000L)
                if (diffDays >= 30) {
                    count++
                    dao.insertNotification(NotificationLocal(
                        userId = user.uid,
                        title = "MedAI sizni sog'indi 💚",
                        message = "Sog'lig'ingizni tekshirib ko'rdingizmi? MedAI siz bilan! 💚",
                        type = "system"
                    ))
                }
            }
            logAdminAction("Broadcast to Cold Users", "All Cold Users", "$count ta sovuq foydalanuvchiga eslatma xabari yuborildi.")
            Toast.makeText(getApplication(), "$count ta foydalanuvchiga xabar yuborildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPremiumExpiryReminder(userId: String, name: String, daysLeft: Long) {
        viewModelScope.launch {
            dao.insertNotification(NotificationLocal(
                userId = userId,
                title = "⏰ Premium muddati tugamoqda",
                message = "⏰ Premiumingiz $daysLeft kunda tugaydi! Yangilang: 30,000 so'm",
                type = "premium"
            ))
            logAdminAction("Send Premium Expiry Reminder", name, "Foydalanuvchiga $daysLeft kunlik premium eslatmasi yuborildi.")
            Toast.makeText(getApplication(), "Eslatma yuborildi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun extendPremium(uid: String, name: String, days: Int = 30) {
        viewModelScope.launch {
            val users = dao.getAllSystemUsers()
            val user = users.find { it.uid == uid }
            if (user != null) {
                val currentExpiry = user.premiumExpiry ?: System.currentTimeMillis()
                val newExpiry = currentExpiry + (days * 24 * 3600 * 1000L)
                user.isPremium = true
                user.premiumExpiry = newExpiry
                dao.insertSystemUser(user)
                
                val curr = dao.getCurrentUser()
                if (curr != null && curr.uid == uid) {
                    dao.insertUser(curr.copy(isPremium = true, premiumExpiry = newExpiry))
                }
                
                dao.insertNotification(NotificationLocal(
                    userId = uid,
                    title = "Premium uzaytirildi! 🎉",
                    message = "Sizning Premium obunangiz admin tomonidan yana $days kunga uzaytirildi! Rahmat!",
                    type = "premium"
                ))
                
                logAdminAction("Extend Premium", name, "Premium obunasi $days kunga uzaytirildi.")
                Toast.makeText(getApplication(), "Premium $days kunga uzaytirildi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun blockUser(uid: String, name: String, reason: String) {
        viewModelScope.launch {
            val users = dao.getAllSystemUsers()
            val user = users.find { it.uid == uid }
            if (user != null) {
                user.isBanned = true
                dao.insertSystemUser(user)
            }
            
            val curr = dao.getCurrentUser()
            if (curr != null && curr.uid == uid) {
                dao.insertUser(curr.copy(isBanned = true))
            }
            
            logAdminAction("Block User", name, "Bloklash sababi: $reason")
            Toast.makeText(getApplication(), "$name bloklandi", Toast.LENGTH_SHORT).show()
        }
    }

    fun unblockUser(uid: String, name: String) {
        viewModelScope.launch {
            val users = dao.getAllSystemUsers()
            val user = users.find { it.uid == uid }
            if (user != null) {
                user.isBanned = false
                dao.insertSystemUser(user)
            }
            
            val curr = dao.getCurrentUser()
            if (curr != null && curr.uid == uid) {
                dao.insertUser(curr.copy(isBanned = false))
            }
            
            logAdminAction("Unblock User", name, "Foydalanuvchi blokdan chiqarildi")
            Toast.makeText(getApplication(), "$name blokdan chiqarildi", Toast.LENGTH_SHORT).show()
        }
    }

    fun trackScreenOpen(screenName: String) {
        viewModelScope.launch {
            val curr = dao.getCurrentUser()
            if (curr != null) {
                val sysUser = dao.getAllSystemUsers().find { it.uid == curr.uid }
                if (sysUser != null) {
                    sysUser.currentScreen = screenName
                    sysUser.lastActive = System.currentTimeMillis()
                    dao.insertSystemUser(sysUser)
                } else {
                    val newSys = UserSystem(
                        uid = curr.uid,
                        name = curr.name,
                        email = curr.email,
                        phone = curr.phone,
                        dateOfBirth = curr.dateOfBirth,
                        gender = curr.gender,
                        bloodType = curr.bloodType,
                        height = curr.height,
                        weight = curr.weight,
                        isPremium = curr.isPremium,
                        premiumExpiry = curr.premiumExpiry,
                        language = curr.language,
                        fcmToken = curr.fcmToken,
                        createdAt = curr.createdAt,
                        lastActive = System.currentTimeMillis(),
                        healthScore = curr.healthScore,
                        isAdmin = curr.isAdmin,
                        isBanned = curr.isBanned,
                        avatarUrl = curr.avatarUrl,
                        currentScreen = screenName,
                        city = "Toshkent"
                    )
                    dao.insertSystemUser(newSys)
                }
                
                dao.insertUserActivity(UserActivityLog(
                    userId = curr.uid,
                    screenName = screenName,
                    actionType = "view_screen",
                    details = "Opened screen: $screenName"
                ))
            }
        }
    }

    fun checkPremiumExpiriesAndProcess() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val users = dao.getAllSystemUsers()
            users.forEach { user ->
                if (user.isPremium && user.premiumExpiry != null) {
                    val expiry = user.premiumExpiry!!
                    val diffMs = expiry - now
                    if (diffMs <= 0) {
                        user.isPremium = false
                        user.premiumExpiry = null
                        dao.insertSystemUser(user)
                        
                        val curr = dao.getCurrentUser()
                        if (curr != null && curr.uid == user.uid) {
                            dao.insertUser(curr.copy(isPremium = false, premiumExpiry = null))
                        }
                        
                        dao.insertNotification(NotificationLocal(
                            userId = user.uid,
                            title = "Premium obunangiz tugadi ⚠️",
                            message = "Premium xizmatlar muddati tugadi va bepul rejaga o'tkazildingiz. Yangilash uchun to'lov qiling.",
                            type = "premium"
                        ))
                        
                        dao.insertAdminLog(AdminLog(
                            adminEmail = "system_cron",
                            action = "Auto Downgrade",
                            targetUser = user.name,
                            details = "Muddati o'tgan premium obuna avtomatik ravishda bekor qilindi."
                        ))
                    }
                }
            }
        }
    }

    private suspend fun seedAdminDataIfNeeded() {
        val flags = dao.getFeatureFlagsConfig()
        if (flags == null) {
            dao.insertFeatureFlagsConfig(FeatureFlagsConfig())
        }

        val version = dao.getAppVersionConfig()
        if (version == null) {
            dao.insertAppVersionConfig(AppVersionConfig())
        }

        val announcement = dao.getAnnouncementConfig()
        if (announcement == null) {
            dao.insertAnnouncementConfig(AnnouncementConfig(
                active = true,
                textUz = "Diqqat! MedAI xizmatlari sizga muntazam salomatlik maslahatlarini taqdim etadi.",
                textRu = "Внимание! Услуги MedAI регулярно предоставляют вам советы по здоровью.",
                textEn = "Attention! MedAI services regularly provide you with health tips.",
                color = "yellow"
            ))
        }

        val banner = dao.getPopupBannerConfig()
        if (banner == null) {
            dao.insertPopupBannerConfig(PopupBannerConfig(
                active = false,
                imageUrl = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=600&q=80",
                titleUz = "Yangi aksiya! 🎉",
                titleRu = "Новая акция! 🎉",
                titleEn = "New Promo! 🎉",
                subtitleUz = "Premium a'zolikka o'ting va barcha imkoniyatlardan cheksiz foydalaning!",
                subtitleRu = "Перейдите на Премиум и пользуйтесь всеми функциями без ограничений!",
                subtitleEn = "Upgrade to Premium and enjoy unlimited access to all features!",
                buttonTextUz = "Premiumga o'tish",
                buttonTextRu = "Перейти на Премиум",
                buttonTextEn = "Upgrade Now",
                actionType = "open_screen",
                actionValue = "upgrade"
            ))
        }

        val currentDiseases = dao.getAllDiseasesFlow().first()
        if (currentDiseases.isEmpty()) {
            val diseaseList = listOf(
                DiseaseEntry(
                    nameUz = "Gripp",
                    nameRu = "Грипп",
                    nameEn = "Influenza (Flu)",
                    symptomsUz = "Isitma, bosh og'rig'i, yo'tal, charchoq, tomoq og'rig'i",
                    descriptionUz = "O'tkir yuqumli nafas yo'llari kasalligi bo'lib, gripp virusi keltirib chiqaradi.",
                    descriptionRu = "Острое инфекционное заболевание дыхательных путей, вызываемое вирусом гриппа.",
                    descriptionEn = "An acute infectious respiratory illness caused by influenza viruses.",
                    specialistType = "Terapevt",
                    severity = "moderate"
                ),
                DiseaseEntry(
                    nameUz = "Arterial Gipertenziya",
                    nameRu = "Артериальная Гипертензия",
                    nameEn = "Arterial Hypertension",
                    symptomsUz = "Bosh aylanishi, bosh og'rig'i, qon bosimi oshishi, ko'ngil aynishi",
                    descriptionUz = "Surunkali kasallik bo'lib, arterial qon bosimining turg'un ko'tarilishi bilan xarakterlanadi.",
                    descriptionRu = "Хроническое заболевание, характеризующееся стойким повышением артериального давления.",
                    descriptionEn = "A chronic medical condition in which the blood pressure in the arteries is persistently elevated.",
                    specialistType = "Kardiolog",
                    severity = "severe"
                ),
                DiseaseEntry(
                    nameUz = "Gastrit",
                    nameRu = "Гастрит",
                    nameEn = "Gastritis",
                    symptomsUz = "Oshqozonda og'rig'i, jig'ildon qaynashi, ko'ngil aynishi, ishtahasizlik",
                    descriptionUz = "Oshqozon shilliq qavatining yallig'lanishi.",
                    descriptionRu = "Воспаление слизистой оболочки желудка.",
                    descriptionEn = "Inflammation of the protective lining of the stomach.",
                    specialistType = "Terapevt",
                    severity = "mild"
                )
            )
            diseaseList.forEach { dao.insertDisease(it) }
        }

        val currentMeds = dao.getAllMedicinesFlow().first()
        if (currentMeds.isEmpty()) {
            val medList = listOf(
                MedicineEntry(
                    name = "Paratsetamol",
                    description = "Isitma tushiruvchi va og'riq qoldiruvchi preparat.",
                    dosage = "Kattalarga 500mg, kuniga 2-3 marta",
                    sideEffects = "Allergik reaksiyalar, jigar yuklamasi (doza oshib ketganda)",
                    category = "Og'riq qoldiruvchi"
                ),
                MedicineEntry(
                    name = "Ibuprofen",
                    description = "Yallig'lanishga qarshi, og'riq qoldiruvchi dori vositasi.",
                    dosage = "400mg, ovqatdan so'ng kuniga 2 marta",
                    sideEffects = "Oshqozon og'rig'i, ko'ngil aynishi",
                    category = "Yallig'lanishga qarshi"
                ),
                MedicineEntry(
                    name = "Aspirin",
                    description = "Qonni suyultiruvchi, yallig'lanishga qarshi va og'riq qoldiruvchi.",
                    dosage = "Kuniga 1/4 yoki 1 tabletka",
                    sideEffects = "Qon ketish xavfi, oshqozon shilliq qavatiga ta'sir",
                    category = "Kardiologik"
                )
            )
            medList.forEach { dao.insertMedicine(it) }
        }

        val currentUsers = dao.getAllSystemUsers()
        if (currentUsers.isEmpty()) {
            val mockUsersList = listOf(
                UserSystem(
                    uid = "user_1",
                    name = "Jamshid Alimov",
                    email = "jamshid@gmail.com",
                    phone = "+998 90 345 67 89",
                    dateOfBirth = "1994-11-12",
                    gender = "male",
                    bloodType = "A+",
                    height = 180.0,
                    weight = 82.0,
                    isPremium = true,
                    premiumExpiry = System.currentTimeMillis() + (5 * 24 * 3600 * 1000L),
                    language = "uz",
                    fcmToken = "token_jamshid",
                    createdAt = System.currentTimeMillis() - (20 * 24 * 3600 * 1000L),
                    lastActive = System.currentTimeMillis() - (10 * 60 * 1000L),
                    healthScore = 88,
                    isAdmin = false,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                    currentScreen = "SymptomChecker",
                    city = "Toshkent",
                    lastScreenBeforeUpgrade = "SymptomChecker"
                ),
                UserSystem(
                    uid = "user_2",
                    name = "Sardorbek Karimov",
                    email = "sardor@gmail.com",
                    phone = "+998 91 123 45 67",
                    dateOfBirth = "1997-03-24",
                    gender = "male",
                    bloodType = "O+",
                    height = 175.0,
                    weight = 70.0,
                    isPremium = true,
                    premiumExpiry = System.currentTimeMillis() + (2 * 24 * 3600 * 1000L),
                    language = "uz",
                    fcmToken = "token_sardor",
                    createdAt = System.currentTimeMillis() - (15 * 24 * 3600 * 1000L),
                    lastActive = System.currentTimeMillis() - (2 * 60 * 1000L),
                    healthScore = 92,
                    isAdmin = false,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80",
                    currentScreen = "Home",
                    city = "Samarqand",
                    lastScreenBeforeUpgrade = "AiDoctor"
                ),
                UserSystem(
                    uid = "user_3",
                    name = "Malika Sobirova",
                    email = "malika@gmail.com",
                    phone = "+998 93 987 65 43",
                    dateOfBirth = "2001-08-14",
                    gender = "female",
                    bloodType = "B-",
                    height = 165.0,
                    weight = 54.0,
                    isPremium = false,
                    premiumExpiry = null,
                    language = "uz",
                    fcmToken = "token_malika",
                    createdAt = System.currentTimeMillis() - (5 * 24 * 3600 * 1000L),
                    lastActive = System.currentTimeMillis() - (4 * 24 * 3600 * 1000L),
                    healthScore = 79,
                    isAdmin = false,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                    currentScreen = "MedicineSearch",
                    city = "Buxoro",
                    lastScreenBeforeUpgrade = ""
                ),
                UserSystem(
                    uid = "user_4",
                    name = "Jasur Rustamov",
                    email = "jasur@gmail.com",
                    phone = "+998 97 765 43 21",
                    dateOfBirth = "1992-06-30",
                    gender = "male",
                    bloodType = "AB+",
                    height = 182.0,
                    weight = 89.0,
                    isPremium = false,
                    premiumExpiry = null,
                    language = "uz",
                    fcmToken = "token_jasur",
                    createdAt = System.currentTimeMillis() - (40 * 24 * 3600 * 1000L),
                    lastActive = System.currentTimeMillis() - (12 * 24 * 3600 * 1000L),
                    healthScore = 65,
                    isAdmin = false,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                    currentScreen = "Reminders",
                    city = "Namangan",
                    lastScreenBeforeUpgrade = ""
                ),
                UserSystem(
                    uid = "user_5",
                    name = "Zilola Toirova",
                    email = "zilola@gmail.com",
                    phone = "+998 94 444 33 22",
                    dateOfBirth = "1995-10-05",
                    gender = "female",
                    bloodType = "O-",
                    height = 168.0,
                    weight = 59.0,
                    isPremium = false,
                    premiumExpiry = null,
                    language = "uz",
                    fcmToken = "token_zilola",
                    createdAt = System.currentTimeMillis() - (60 * 24 * 3600 * 1000L),
                    lastActive = System.currentTimeMillis() - (45 * 24 * 3600 * 1000L),
                    healthScore = 55,
                    isAdmin = false,
                    isBanned = false,
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
                    currentScreen = "Profile",
                    city = "Andijon",
                    lastScreenBeforeUpgrade = ""
                )
            )
            dao.insertSystemUsers(mockUsersList)

            val mockActivities = listOf(
                UserActivityLog(userId = "user_1", screenName = "Home", actionType = "view_screen", details = "Bosh ekranga kirdi"),
                UserActivityLog(userId = "user_1", screenName = "SymptomChecker", actionType = "view_screen", details = "Simptomlar bo'limiga kirdi"),
                UserActivityLog(userId = "user_1", screenName = "SymptomChecker", actionType = "symptom_check", details = "Isitma va tomoq og'rig'i bo'yicha tashxis o'tkazdi"),
                UserActivityLog(userId = "user_2", screenName = "Home", actionType = "view_screen", details = "Bosh ekranga kirdi"),
                UserActivityLog(userId = "user_2", screenName = "AiDoctor", actionType = "view_screen", details = "AI Shifokor bilan suhbatni boshladi"),
                UserActivityLog(userId = "user_3", screenName = "MedicineSearch", actionType = "view_screen", details = "Dori qidirish bo'limiga kirdi"),
                UserActivityLog(userId = "user_3", screenName = "MedicineSearch", actionType = "medicine_search", details = "Paratsetamol preparatini qidirdi"),
                UserActivityLog(userId = "user_4", screenName = "Reminders", actionType = "view_screen", details = "Eslatmalar oynasini ochdi"),
                UserActivityLog(userId = "user_4", screenName = "Reminders", actionType = "reminder_complete", details = "Vitamin D eslatmasini bajardi")
            )
            mockActivities.forEach { dao.insertUserActivity(it) }

            val mockErrors = listOf(
                ErrorLog(userId = "user_1", screen = "SymptomChecker", errorMessage = "Network timeout exception while calling Gemini API", appVersion = "1.1.2", deviceInfo = "Xiaomi Redmi Note 11", isResolved = false),
                ErrorLog(userId = "user_3", screen = "LabAnalysis", errorMessage = "NullPointerException inside analyzeLabReportImage()", appVersion = "1.1.2", deviceInfo = "Samsung Galaxy S22", isResolved = false),
                ErrorLog(userId = "user_4", screen = "Home", errorMessage = "SQLiteException: no such column", appVersion = "1.1.1", deviceInfo = "Realme 9 Pro", isResolved = true)
            )
            mockErrors.forEach { dao.insertErrorLog(it) }

            val mockApiLogs = listOf(
                ApiUsageLog(feature = "Symptom", tokensUsed = 1250, costEstimateUsd = 0.0025, timestamp = System.currentTimeMillis() - (2 * 3600 * 1000)),
                ApiUsageLog(feature = "Doctor", tokensUsed = 2400, costEstimateUsd = 0.0048, timestamp = System.currentTimeMillis() - (5 * 3600 * 1000)),
                ApiUsageLog(feature = "Lab", tokensUsed = 4100, costEstimateUsd = 0.0082, timestamp = System.currentTimeMillis() - (12 * 3600 * 1000)),
                ApiUsageLog(feature = "Medicine", tokensUsed = 850, costEstimateUsd = 0.0017, timestamp = System.currentTimeMillis() - (24 * 3600 * 1000))
            )
            mockApiLogs.forEach { dao.insertApiUsageLog(it) }

            val mockAdminLogs = listOf(
                AdminLog(adminEmail = SUPER_ADMIN_EMAIL, action = "System Setup", targetUser = "All", details = "Tizim ma'lumotlari muvaffaqiyatli o'rnatildi.", timestamp = System.currentTimeMillis() - (24 * 3600 * 1000L))
            )
            mockAdminLogs.forEach { dao.insertAdminLog(it) }
        }
    }

    private var adminLogsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private fun startAdminLogsFirestoreListener() {
        try {
            if (com.google.firebase.FirebaseApp.getApps(getApplication<Application>()).isEmpty()) return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            adminLogsListenerRegistration?.remove()
            adminLogsListenerRegistration = db.collection("adminLogs")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("FirestoreListener", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documentChanges) {
                                if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val data = doc.document.data
                                    val email = data["adminEmail"] as? String ?: ""
                                    val action = data["action"] as? String ?: ""
                                    val target = data["targetUser"] as? String ?: ""
                                    val details = data["details"] as? String ?: ""
                                    val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()

                                    // Verify duplicate
                                    val exists = dao.getAdminLogByDetails(email, action, timestamp)
                                    if (exists == null) {
                                        dao.insertAdminLog(AdminLog(
                                            adminEmail = email,
                                            action = action,
                                            targetUser = target,
                                            details = details,
                                            timestamp = timestamp
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Throwable) {
            Log.e("FirestoreListener", "Failed to start adminLogs listener: ${e.message}")
        }
    }

    // --- Firestore Medication Reminders ---
    private var remindersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun startFirestoreRemindersListener(userId: String) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(getApplication<Application>()).isEmpty()) return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            remindersListenerRegistration?.remove()
            remindersListenerRegistration = db.collection("medicationReminders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirestoreReminders", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = mutableListOf<FirestoreMedicationReminder>()
                        for (doc in snapshot.documents) {
                            try {
                                val completedDatesRaw = doc.get("completedDates") as? List<*>
                                val completedDates = completedDatesRaw?.mapNotNull { it?.toString() } ?: emptyList()

                                val rem = FirestoreMedicationReminder(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    medicineName = doc.getString("medicineName") ?: "",
                                    dosage = doc.getString("dosage") ?: "",
                                    time = doc.getString("time") ?: "",
                                    frequency = doc.getString("frequency") ?: "",
                                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                                    notificationFrequency = doc.getString("notificationFrequency") ?: "Exact time",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    targetFamilyMember = doc.getString("targetFamilyMember"),
                                    notes = doc.getString("notes"),
                                    completedDates = completedDates,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                )
                                list.add(rem)
                            } catch (ex: Exception) {
                                Log.e("FirestoreReminders", "Error parsing doc", ex)
                            }
                        }
                        list.sortByDescending { it.timestamp }
                        _firestoreReminders.value = list
                    }
                }
        } catch (e: Throwable) {
            Log.e("FirestoreReminders", "Failed to start listener: ${e.message}")
        }
    }

    // --- Firestore Vitals Listener & Logging ---
    private var vitalsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun startFirestoreVitalsListener(userId: String) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(getApplication<Application>()).isEmpty()) return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            vitalsListenerRegistration?.remove()
            vitalsListenerRegistration = db.collection("vitals")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirestoreVitals", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        if (snapshot.isEmpty) {
                            _firestoreVitals.value = emptyList()
                            return@addSnapshotListener
                        }
                        val list = mutableListOf<com.example.data.FirestoreVitalReading>()
                        for (doc in snapshot.documents) {
                            try {
                                val reading = com.example.data.FirestoreVitalReading(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    date = doc.getString("date") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    heartRate = doc.getLong("heartRate")?.toInt() ?: 0,
                                    bpSystolic = doc.getLong("bpSystolic")?.toInt() ?: 0,
                                    bpDiastolic = doc.getLong("bpDiastolic")?.toInt() ?: 0,
                                    weight = doc.getDouble("weight") ?: 0.0,
                                    note = doc.getString("note") ?: ""
                                )
                                list.add(reading)
                            } catch (ex: Exception) {
                                Log.e("FirestoreVitals", "Error parsing doc", ex)
                            }
                        }
                        list.sortBy { it.date }
                        _firestoreVitals.value = list
                    }
                }
        } catch (e: Throwable) {
            Log.e("FirestoreVitals", "Failed to start listener: ${e.message}")
        }
    }

    fun seedMockVitalsInFirestore(userId: String) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val vitalsColl = db.collection("vitals")
            
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayMs = System.currentTimeMillis()
            
            val mockReadings = listOf(
                Triple(-6, Pair(72, Pair(118, 79)), Pair(71.2, "Yaxshi")),
                Triple(-5, Pair(75, Pair(121, 81)), Pair(71.0, "Maksimal faollik")),
                Triple(-4, Pair(68, Pair(119, 78)), Pair(70.8, "Yaxshi uyqu")),
                Triple(-3, Pair(82, Pair(125, 83)), Pair(71.1, "Kofe ichildi")),
                Triple(-2, Pair(70, Pair(117, 77)), Pair(70.5, "Dam olish")),
                Triple(-1, Pair(74, Pair(120, 80)), Pair(70.6, "Normal holat")),
                Triple(0, Pair(73, Pair(119, 79)), Pair(70.4, "Vazn o'lchandi"))
            )
            
            for (mock in mockReadings) {
                val ms = todayMs + mock.first * 24L * 3600L * 1000L
                val dateStr = sdf.format(java.util.Date(ms))
                val docId = "vitals_${userId}_$dateStr"
                
                val data = hashMapOf(
                    "userId" to userId,
                    "date" to dateStr,
                    "timestamp" to ms,
                    "heartRate" to mock.second.first,
                    "bpSystolic" to mock.second.second.first,
                    "bpDiastolic" to mock.second.second.second,
                    "weight" to mock.third.first,
                    "note" to mock.third.second
                )
                vitalsColl.document(docId).set(data)
            }
            Log.d("FirestoreVitals", "Historical vitals seeded successfully")
        } catch (e: Exception) {
            Log.e("FirestoreVitals", "Failed to seed: ${e.message}")
        }
    }

    fun logVitalToFirestore(
        heartRate: Int = 0,
        bpSystolic: Int = 0,
        bpDiastolic: Int = 0,
        weight: Double = 0.0,
        note: String = ""
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateStr = sdf.format(java.util.Date())
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val docId = "vitals_${user.uid}_$dateStr"
                val docRef = db.collection("vitals").document(docId)
                
                docRef.get().addOnSuccessListener { doc ->
                    val existingHR = doc.getLong("heartRate")?.toInt() ?: 0
                    val existingSys = doc.getLong("bpSystolic")?.toInt() ?: 0
                    val existingDia = doc.getLong("bpDiastolic")?.toInt() ?: 0
                    val existingWeight = doc.getDouble("weight") ?: 0.0
                    val existingNote = doc.getString("note") ?: ""

                    val data = hashMapOf(
                        "userId" to user.uid,
                        "date" to dateStr,
                        "timestamp" to System.currentTimeMillis(),
                        "heartRate" to if (heartRate > 0) heartRate else existingHR,
                        "bpSystolic" to if (bpSystolic > 0) bpSystolic else existingSys,
                        "bpDiastolic" to if (bpDiastolic > 0) bpDiastolic else existingDia,
                        "weight" to if (weight > 0.0) weight else existingWeight,
                        "note" to if (note.isNotEmpty()) note else existingNote
                    )

                    docRef.set(data).addOnSuccessListener {
                        Log.d("FirestoreVitals", "Recorded to Firestore successfully")
                    }.addOnFailureListener { e ->
                        Log.e("FirestoreVitals", "Failed to record: ${e.message}")
                    }
                }.addOnFailureListener {
                    val data = hashMapOf(
                        "userId" to user.uid,
                        "date" to dateStr,
                        "timestamp" to System.currentTimeMillis(),
                        "heartRate" to heartRate,
                        "bpSystolic" to bpSystolic,
                        "bpDiastolic" to bpDiastolic,
                        "weight" to weight,
                        "note" to note
                    )
                    docRef.set(data)
                }
            } catch (e: Exception) {
                Log.e("FirestoreVitals", "Error: ${e.message}")
            }
        }
    }

    fun addFirestoreReminder(
        medicineName: String,
        dosage: String,
        time: String,
        frequency: String,
        notificationsEnabled: Boolean,
        notificationFrequency: String,
        targetFamily: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val docRef = db.collection("medicationReminders").document()
                val data = hashMapOf(
                    "userId" to user.uid,
                    "medicineName" to medicineName,
                    "dosage" to dosage,
                    "time" to time,
                    "frequency" to frequency,
                    "notificationsEnabled" to notificationsEnabled,
                    "notificationFrequency" to notificationFrequency,
                    "isActive" to true,
                    "targetFamilyMember" to targetFamily,
                    "notes" to notes,
                    "completedDates" to emptyList<String>(),
                    "timestamp" to System.currentTimeMillis()
                )
                
                docRef.set(data)
                    .addOnSuccessListener {
                        if (notificationsEnabled) {
                            sendSimulatedPush(
                                "⏰ Yangi dori eslatmasi o'rnatildi",
                                "$medicineName ($dosage) - soat $time da. Takrorlanish: $frequency.",
                                "reminder"
                            )
                        }
                        Toast.makeText(getApplication(), "Eslatma Firestore-da muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { ex ->
                        Toast.makeText(getApplication(), "Xatolik: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Log.e("FirestoreReminders", "Failed to add: ${e.message}")
            }
        }
    }

    fun deleteFirestoreReminder(id: String) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("medicationReminders").document(id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(getApplication(), "Eslatma muvaffaqiyatli o'chirildi!", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e("FirestoreReminders", "Failed to delete: ${e.message}")
            }
        }
    }

    fun toggleFirestoreReminderActive(id: String, currentActive: Boolean) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("medicationReminders").document(id)
                    .update("isActive", !currentActive)
            } catch (e: Exception) {
                Log.e("FirestoreReminders", "Failed to toggle: ${e.message}")
            }
        }
    }

    fun completeFirestoreReminder(id: String, completedDates: List<String>) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (completedDates.contains(todayStr)) return@launch // Already completed for today
            
            val updatedDates = completedDates + todayStr
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("medicationReminders").document(id)
                    .update("completedDates", updatedDates)
                    .addOnSuccessListener {
                        checkCompletedReminderAchievements()
                        recalculateHealthScore()
                        Toast.makeText(getApplication(), "Dori qabul qilingani belgilandi! 💊", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e("FirestoreReminders", "Failed to complete: ${e.message}")
            }
        }
    }

    private var paymentRequestsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Admins mirror every request (for the review list); regular users may only query their
    // own (firestore.rules rejects an unfiltered list query from a non-admin outright). When a
    // request the current device itself submitted flips to "approved"/"rejected" here, this is
    // what actually grants premium / notifies the real requester's device — previously that
    // only ever happened on whichever device the admin used to call approvePayment().
    private fun startPaymentRequestsFirestoreListener(userId: String, isAdmin: Boolean) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(getApplication<Application>()).isEmpty()) return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            paymentRequestsListenerRegistration?.remove()
            var query: com.google.firebase.firestore.Query = db.collection("paymentRequests")
            if (!isAdmin) {
                query = query.whereEqualTo("userId", userId)
            }
            paymentRequestsListenerRegistration = query
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("FirestoreListener", "Payment listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documentChanges) {
                                val data = doc.document.data
                                val id = doc.document.id
                                val requestUserId = data["userId"] as? String ?: ""
                                val userName = data["userName"] as? String ?: ""
                                val userEmail = data["userEmail"] as? String ?: ""
                                val userPhone = data["userPhone"] as? String ?: ""
                                val checkImageUrl = data["checkImageUrl"] as? String ?: ""
                                val status = data["status"] as? String ?: "pending"
                                val rejectionReason = data["rejectionReason"] as? String ?: ""
                                val submittedAt = (data["submittedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                                val reviewedAt = (data["reviewedAt"] as? Number)?.toLong()
                                val reviewedBy = data["reviewedBy"] as? String

                                val previous = dao.getPaymentRequestById(id)
                                val request = PaymentRequestLocal(
                                    id = id,
                                    userId = requestUserId,
                                    userName = userName,
                                    userEmail = userEmail,
                                    userPhone = userPhone,
                                    checkImageUrl = checkImageUrl,
                                    status = status,
                                    rejectionReason = rejectionReason,
                                    submittedAt = submittedAt,
                                    reviewedAt = reviewedAt,
                                    reviewedBy = reviewedBy
                                )
                                dao.insertPaymentRequest(request)

                                // Only react on this device if it's the actual requester, and
                                // only on the transition into approved/rejected (not every
                                // re-sync), so premium isn't re-granted/re-notified repeatedly.
                                val justDecided = previous?.status == "pending" && status != "pending"
                                if (!isAdmin && requestUserId == userId && justDecided) {
                                    if (status == "approved") {
                                        val me = dao.getCurrentUser()
                                        if (me != null && me.uid == userId) {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.DAY_OF_YEAR, 30)
                                            dao.insertUser(me.copy(isPremium = true, premiumExpiry = cal.timeInMillis))
                                        }
                                        dao.insertNotification(NotificationLocal(
                                            userId = userId,
                                            title = "🎉 Premium faollashtirildi!",
                                            message = "To'lov tasdiqlandi. Barcha premium imkoniyatlardan cheksiz foydalanishingiz mumkin!",
                                            type = "premium"
                                        ))
                                    } else if (status == "rejected") {
                                        dao.insertNotification(NotificationLocal(
                                            userId = userId,
                                            title = "❌ To'lov rad etildi",
                                            message = "Rad etilish sababi: $rejectionReason. Iltimos, qaytadan urinib ko'ring.",
                                            type = "premium"
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Throwable) {
            Log.e("FirestorePaymentListener", "Failed to start listener: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        documentsJob?.cancel()
        scansJob?.cancel()
        remindersListenerRegistration?.remove()
        vitalsListenerRegistration?.remove()
        adminLogsListenerRegistration?.remove()
        paymentRequestsListenerRegistration?.remove()
    }
}


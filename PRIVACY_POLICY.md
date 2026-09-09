# MedAI — Maxfiylik Siyosati

**Amal qilish sanasi:** 2026-yil 9-sentyabr

Ushbu hujjat MedAI ("ilova", "biz") mobil ilovasi foydalanuvchilarining ("siz") shaxsiy va tibbiy ma'lumotlarini qanday to'plashi, ishlatishi, saqlashi va himoya qilishini tushuntiradi. Ilovani o'rnatish yoki undan foydalanish orqali siz ushbu siyosat shartlariga rozilik bildirasiz.

---

## 1. To'planadigan ma'lumotlar

### 1.1. Hisob va profil ma'lumotlari
Ism, elektron pochta, telefon raqami, tug'ilgan sana, jins, qon guruhi, bo'y, vazn — ro'yxatdan o'tish yoki Google orqali kirishda taqdim etiladi.

### 1.2. Sog'liq ma'lumotlari
Simptom tekshiruvi natijalari, sog'liq balli, kunlik qadamlar, yurak urishi/qon bosimi/vazn ko'rsatkichlari, dori-darmon eslatmalari va ularning tarixi, tibbiy hujjatlar va retsept skanerlari (rasm sifatida yuklanadi).

### 1.3. To'lov tasdiqlash rasmlari
Premium obunani tasdiqlash uchun yuborilgan chek/screenshot rasmlari — faqat to'lovni tekshirish maqsadida saqlanadi.

### 1.4. Joylashuv
Favqulodda yordam (SOS) funksiyasi uchun taxminiy joylashuv ma'lumoti ishlatilishi mumkin. Ilova aniq GPS joylashuvni faqat siz ruxsat bergan holatlarda so'raydi.

### 1.5. Texnik ma'lumotlar
Qurilma modeli, OS versiyasi, ilova versiyasi, xatolik jurnallari (error logs) va API foydalanish statistikasi — ilova barqarorligini yaxshilash uchun avtomatik yig'iladi.

---

## 2. Ma'lumotlardan qanday foydalanamiz

- Ilova funksiyalarini ta'minlash (simptom tahlili, dori eslatmalari, oila monitoringi va h.k.)
- Sun'iy intellekt asosida tibbiy maslahat va tahlil taqdim etish
- Premium obunani boshqarish va to'lovlarni tasdiqlash
- Xatoliklarni aniqlash va ilova sifatini oshirish
- Favqulodda vaziyatlarda oila a'zolariga xabar berish (agar siz shu funksiyadan foydalansangiz)

Biz sizning ma'lumotlaringizni **hech qachon uchinchi tomonlarga sotmaymiz** va reklama maqsadida ishlatmaymiz.

---

## 3. Sun'iy intellekt (Google Gemini) bilan ma'lumot almashish

Simptom tahlili, AI Shifokor suhbati va retsept/lab tahlili funksiyalari sizning matningiz va (agar yuklasangiz) rasmlaringizni tahlil qilish uchun **Google Gemini API**siga yuboradi. Bu ma'lumotlar Google'ning o'z maxfiylik siyosati asosida qayta ishlanadi. Agar Gemini xizmati mavjud bo'lmasa, ilova oldindan tayyorlangan umumiy maslahat bilan javob beradi (sizning shaxsiy ma'lumotlaringiz yuborilmaydi).

---

## 4. Ma'lumotlarni saqlash va xavfsizlik

- Ma'lumotlar qurilmangizda mahalliy (Room) bazada va bulutli **Firebase Firestore**da saqlanadi.
- Firestore xavfsizlik qoidalari har bir foydalanuvchi faqat **o'z** ma'lumotlarini o'qishi/yozishi mumkinligini ta'minlaydi; administrator huquqi server tomonidan tasdiqlangan **Firebase custom claim** orqali beriladi — mijoz ilovasidagi hech qanday sozlama bu huquqni bera olmaydi.
- To'lov va admin harakatlari audit jurnali (kim, qachon, nima qildi) yuritiladi.

---

## 5. Google orqali kirish

Ilovaga Google hisobingiz orqali kirishingiz mumkin (Firebase Authentication). Biz Google hisobingizdan faqat ism va elektron pochta manzilini olamiz — parolingizga kirish imkonimiz yo'q.

---

## 6. Ma'lumotlarni saqlash muddati

Hisobingiz faol bo'lgan davomida ma'lumotlaringiz saqlanadi. Hisobni o'chirishni so'raganingizda, tegishli ma'lumotlar mahalliy va bulutli bazalardan o'chiriladi (audit/moliyaviy hujjatlar qonuniy talablarga ko'ra qisqa muddat saqlanishi mumkin).

---

## 7. Sizning huquqlaringiz

Siz quyidagi huquqlarga egasiz:
- O'zingiz haqingizda qanday ma'lumot saqlanganini bilish
- Ma'lumotlaringizni tuzatish yoki yangilash (Profil bo'limi orqali)
- Hisobingizni va unga tegishli ma'lumotlarni o'chirishni so'rash
- Til sozlamalarini o'zgartirish (uz/ru/en)

Bu huquqlardan foydalanish uchun quyidagi bo'limdagi aloqa ma'lumotlaridan foydalaning.

---

## 8. Bolalar maxfiyligi

MedAI 16 yoshdan kichik bolalarga mo'ljallanmagan va ular haqida ataylab ma'lumot to'plamaydi.

---

## 9. Ushbu siyosatga o'zgartirishlar

Ushbu siyosat vaqti-vaqti bilan yangilanishi mumkin. Muhim o'zgarishlar haqida ilova ichida bildirishnoma orqali xabar beramiz.

---

## 10. Biz bilan bog'lanish

Savol yoki so'rovlaringiz bo'lsa: **t.me/Medai_support**

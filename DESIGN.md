# MedAI dizayn tizimi

Bu hujjat ilovaning ko'rinishi qanday qurilganini va yangi ekran qo'shganda nimaga amal qilish kerakligini
tushuntiradi. Maqsad: **tinch, ishonchli, tibbiy** ko'rinish. Hamma ekran bir xil tilda "gapirsin".

## 1. Tamoyillar

1. **Bitta aksent — teal.** Binafsha faqat *to'lov holati* (Premium/trial) uchun. Qizil faqat *xavf* (SOS, o'chirish,
   xato) uchun. Qolgan hamma narsa neytral (slate) va teal tonlari.
2. **Tekis kartalar.** Karta = oq (qorong'i temada to'q) sirt + 1dp ingichka chegara. Soya faqat havoda turuvchi
   narsada (dialog, suzuvchi element). Gradient faqat **Bosh sahifa hero kartasi** va **Login sarlavhasi**da.
3. **Ierarxiya.** Ekranda bitta asosiy harakat. Sarlavha → bo'lim sarlavhasi → karta. Ortiqcha ikonka, ortiqcha
   rang yo'q.
4. **Hamma til sig'sin.** UZ, RU, EN. Matn o'rab o'tadi (2 qator), kesilmaydi. Qattiq kodlangan qisqa kenglik yo'q.
5. **Hamma foydalanuvchi uchun.** Teginish maydoni ≥ 48dp, matn kontrasti ≥ 4,5:1 (WCAG AA), shrift ≥ 12sp,
   har ikonka tugmada `contentDescription`.
6. **Yengil harakat.** Bosganda 120ms masshtab (0,97), rang almashinuvi 150–180ms. Cheksiz animatsiya faqat
   yuklanish spinnerida. Tebranish, "pulse", sakrash yo'q.

## 2. Tokenlar

Hammasi `ui/theme/` da. **Rangni hech qachon `Color(0xFF...)` bilan yozmang** — rol ishlating:

```kotlin
val c = MedAITheme.colors          // @Composable ichida
c.canvas, c.surface, c.surfaceSunken, c.surfaceRaised
c.border (ingichka chiziq), c.borderStrong (kiritish chegarasi, 3:1), c.divider
c.textPrimary, c.textSecondary
c.brand / c.onBrand / c.brandStrong / c.brandSoft / c.onBrandSoft
c.premium / c.onPremium / c.premiumSoft / c.onPremiumSoft
c.success|warning|danger|info  (+ ...Soft, on...Soft)  // danger: onDanger
c.tintTeal | tintPeach | tintSky | tintViolet          // MedAITint(bg, fg) — ikonka disklari uchun
c.heroBrush, c.onHero, c.onHeroMuted
```

- **Tipografiya:** `MaterialTheme.typography.*` (headlineMedium, titleLarge, titleMedium, titleSmall, bodyLarge,
  bodyMedium, bodySmall, labelLarge, labelMedium). Qo'shimcha: `MedAIText.MetricLarge/MetricMedium/MetricSmall/Eyebrow`.
  `fontSize = NN.sp` ni qo'lda yozmang (istisno: logotip).
- **Bo'shliq:** chekka 16dp, bo'limlar orasi 16–24dp, karta ichi 16dp. `Spacing.*`.
- **Radius:** `MedAICorners.card` (20), `control` (14), `tile` (18), `hero` (28), `sheet` (28), `pill`.
- **Teginish:** `MinTouch` = 48dp.

## 3. Komponentlar (`ui/MedAIKit.kt`)

| Kerak | Ishlating |
|---|---|
| Asosiy tugma | `MedAIPrimaryButton(text, onClick, icon=, enabled=, loading=)` |
| Ikkinchi darajali | `MedAISecondaryButton` |
| Xavfli/SOS | `MedAIDangerButton` |
| Matnli tugma | `MedAITextButton` |
| Karta | `MedAICard(onClick=?, contentPadding=)` |
| Hero karta | `MedAIHeroCard` + `MedAIMetricPill` |
| Tezkor katak | `MedAIQuickTileGrid(listOf(MedAIQuickItem(...)))` |
| Ro'yxat qatori | `MedAIListRow(icon, title, subtitle, onClick, tint=)` (ichida `MedAICard(contentPadding = 0.dp)`) |
| Filtr chip | `MedAIFilterChip(text, selected, onClick)` |
| Nishon | `MedAIBadge(text, MedAIBadgeTone.X)` |
| Kiritish | `MedAITextField(value, onValueChange, label, ...)` (yorliq tepada) |
| Yuqori panel | `AppHeader(title, onBack, actions)` (tekis, canvas fon, ingichka chiziq) |
| Pastki panel | `MedAIBottomBar` |
| Dialog | `MedAIDialog(title, message, confirmText, onConfirm, onDismissRequest, dismissText=, destructive=, icon=)` |
| Yuklanish | `MedAISpinner`, `MedAISkeleton` |
| Bo'sh holat | `MedAIEmptyState` |
| Xabar bannerи | `MedAIInfoBanner(text, tone=)` |
| Bo'lim sarlavhasi | `MedAISectionHeader` yoki `Text(style = titleMedium)` |

Eski `MedicalCard`, `MedicalButton`, `MedicalTextField`, `MedicalSecondaryButton`, `MedicalDangerButton`
shu kit ustidagi yupqa o'ramlar (imzolari o'zgarmagan) — yangi kodda to'g'ridan-to'g'ri `MedAI*` ishlating.

## 4. Ekran anatomiyasi

```
Scaffold(containerColor = c.canvas, topBar = { AppHeader(title, onBack) }) { padding ->
  LazyColumn/Column(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), spacedBy(16.dp)) {
     bo'lim sarlavhasi (titleMedium)
     MedAICard { ... }            // yoki MedAICard(contentPadding = 0.dp) { MedAIListRow ... }
     ...
     MedAIPrimaryButton(...)      // eng pastda yoki hero ichida
  }
}
```

- Uzun ro'yxat = `LazyColumn` (+ `key`), qisqa forma = `Column(verticalScroll)`.
- `AlertDialog` o'rniga `MedAIDialog`. `Switch` — Material3 (rangi temadan).
- Bo'sh ro'yxat = `MedAIEmptyState` (ikonka, sarlavha, 1 qator tushuntirish, ixtiyoriy harakat).
- Xato holati = `MedAIInfoBanner(tone = MedAITone.Error)` yoki maydon ostida `error`.

## 5. Mantiqqa tegmaslik (qat'iy)

UI qayta yoziladi, **xatti-harakat emas**: ViewModel chaqiruvlari, `collectAsState` manbalari, navigatsiya
marshrutlari va callback'lar, validatsiya, shartlar (premium/trial/admin), DAO — o'zgarmaydi. Yangi UI-holat
(masalan "yuklanmoqda" belgisi) mumkin, lekin natijaga ta'sir qiluvchi mantiq qo'shilmaydi.

## 6. Tarjima

Yangi matn **UZ, RU, EN** da bo'lishi shart. `Translations.getString(key, lang)` mavjud kalitlari yoki ekranda
allaqachon bor `getLangText(uz, ru, en)` namunasi. Qattiq kodlangan bir tilli matn qoldirmang.

## 7. Tekshirish

```
# kompilyatsiya
gradle :app:compileDebugKotlin
# barcha ekran skrinshoti (yorug' + qorong'i), namunaviy ma'lumot bilan
SHOTS_DIR=screens/current gradle :app:testDebugUnitTest --tests 'com.example.catalog.*' -Proborazzi.test.record=true
# 48dp va kesilgan matn tekshiruvi: com.example.stage3.LayoutChecks
# kontrast: com.example.ui.theme.MedAIColorsContrastTest
```

Testlar `com.example.catalog.SampleData.seededVm(lang)` bilan to'ldirilgan ViewModel oladi.
Ma'lum yiqiladigan eski testlar: `PasswordHasherTest` (6), `DataIsolationTest` (4) — ularga tegilmaydi.

## 8. Samaradorlik qoidalari

- Ro'yxatlarda `LazyColumn` + `key`; `forEach` bilan yuzlab element chizmang.
- Qimmat hisob (`sortedBy`, `filter`, JSON parse) `remember(keys)` ichida.
- `Modifier.drawBehind`/`graphicsLayer` da vaqt/holat o'qishda lambda shaklidan foydalaning.
- Cheksiz animatsiya yo'q (spinner bundan mustasno). Rasm — `Coil` bilan, o'lchami cheklangan.

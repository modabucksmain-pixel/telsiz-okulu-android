# Katkı Rehberi

Telsiz Okulu'na katkıda bulunmak istediğin için teşekkürler! 📻

## Nasıl katkı sağlanır

1. Repoyu **fork** et
2. Özellik dalı aç: `git checkout -b ozellik/aciklayici-isim`
3. Değişikliğini yap, derlenip çalıştığından emin ol
4. Anlamlı bir commit mesajı yaz (aşağıdaki biçim)
5. **Pull Request** aç, ne değiştirdiğini ve nedenini açıkla

## İçerik ekleme (kod gerekmez)

En kolay katkı: soru ve kart eklemek. Kod yazmana gerek yok.

- İlgili dosyayı düzenle: `app/src/main/assets/data/*.json`
- Mevcut girdilerin biçimini birebir takip et
- Yeni soru tipi eklemiyorsan var olan `type` değerlerinden birini kullan
- Türkçe karakter kullanırken dosyayı **UTF-8** olarak kaydet
- Eklemeden sonra `./gradlew :app:assembleDebug` ile derlenip açıldığını doğrula

### Soru tipleri

`coktan_secmeli`, `dogru_yanlis`, `bosluk_doldur`, `sayisal`, `hesapla`, `dinle_sec`, `eslestir`, `sirala`, `kelime_hece`, `hizli_tur`, `harfi_yaz`, `kodu_yaz`

## Kod katkısı

- **Dil/UI:** Kotlin + Jetpack Compose, Material 3
- **Mimari:** MVVM (ViewModel + StateFlow). Yeni ekranlar `ui/screens`, durum yönetimi `ui/viewmodel` altına
- DI çerçevesi yok — bağımlılıklar `AppNavGraph` içinde elle kuruluyor
- Yeni renk/stil eklerken `ui/theme` altındaki mevcut paleti kullan (neon mavi/mor/yeşil)
- Görsel eklerken SVG tercih et, `assets/img/svg/` altına koy, tema renklerine uy

### Mimari notları (önemli tuzaklar)

- **Gson null tuzağı:** JSON'da olmayan non-null `String`/`List` alanlara Gson `null` enjekte eder. Yeni alan eklerken `CurriculumRepository` içindeki güvenli-cast (`as?`) bloğunu güncelle.
- **LazyColumn içine LazyVerticalGrid koyma** — çökme geçmişi var.
- Daha fazla ayrıntı için `CLAUDE.md` dosyasına bak.

## Commit biçimi

[Conventional Commits](https://www.conventionalcommits.org/) kullanıyoruz:

```
feat: yeni özellik
fix: hata düzeltme
docs: dokümantasyon
refactor: davranışı değiştirmeyen kod düzenleme
chore: derleme/araç değişiklikleri
```

Konu satırı ≤ 70 karakter, gövde "neden"i anlatsın.

## Hata bildirimi

Issue açarken şunları ekle:
- Cihaz + Android sürümü
- Adım adım yeniden üretme
- Beklenen vs. gözlenen davranış
- Mümkünse ekran görüntüsü

Teşekkürler! 73 📡

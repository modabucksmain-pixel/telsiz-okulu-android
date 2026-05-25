// Curriculum data — simplified from android_native/app/src/main/assets/data/curriculum.json
window.CURRICULUM = [
  {
    id: 'b1', no: 1, kod: 'NATO', baslik: 'NATO Alfabesi',
    aciklama: 'Uluslararası fonetik alfabe ve NATO kodlaması.',
    glyph: 'A·B·C', renk: '#1E3A8A',
    dersler: [
      { id: 'b1l1', baslik: "A'dan G'ye", alt: 'Alpha · Bravo · Charlie · Delta · Echo · Foxtrot · Golf', sure: 6 },
      { id: 'b1l2', baslik: "H'den N'ye", alt: 'Hotel · India · Juliet · Kilo · Lima · Mike · November', sure: 6 },
      { id: 'b1l3', baslik: "O'dan U'ya", alt: 'Oscar · Papa · Quebec · Romeo · Sierra · Tango · Uniform', sure: 6 },
      { id: 'b1l4', baslik: "V'den Z'ye + Rakamlar", alt: 'Victor · Whiskey · X-ray · Yankee · Zulu · 0–9', sure: 7 },
      { id: 'b1q',  baslik: 'Bölüm Sınavı', alt: '15 soru · geçme 60%', sure: 8, sinav: true },
    ],
  },
  {
    id: 'b2', no: 2, kod: 'Q-KODE', baslik: 'Q Kodları',
    aciklama: 'Uluslararası Q kodu kısaltmaları.',
    glyph: 'QRZ', renk: '#0F766E',
    dersler: [
      { id: 'b2l1', baslik: 'Temel Q Kodları', alt: 'QRZ · QSL · QSO · QTH · QRM · QRN', sure: 6 },
      { id: 'b2l2', baslik: 'İletişim Kodları', alt: 'QRB · QRG · QRK · QRL · QRP · QRS · QRT', sure: 6 },
      { id: 'b2l3', baslik: 'Teknik Kodlar', alt: 'QSB · QSK · QSX · QSY · QRO · QRQ', sure: 6 },
      { id: 'b2l4', baslik: 'Diyalog İçinde', alt: 'Gerçek QSO örnekleri', sure: 7 },
      { id: 'b2q',  baslik: 'Bölüm Sınavı', alt: '15 soru · geçme 60%', sure: 8, sinav: true },
    ],
  },
  {
    id: 'b3', no: 3, kod: 'ELEKT', baslik: 'Elektronik Temeller',
    aciklama: 'Volt, amper, ohm, watt; Ohm kanunu, devre elemanları.',
    glyph: 'V=IR', renk: '#B45309',
    dersler: [
      { id: 'b3l1', baslik: 'Temel Kavramlar', alt: 'Volt · Amper · Ohm · Watt', sure: 7 },
      { id: 'b3l2', baslik: 'Ohm Kanunu', alt: 'V = I · R hesaplamaları', sure: 8 },
      { id: 'b3l3', baslik: 'Güç ve Devre', alt: 'P = V · I, direnç, kondansatör, bobin', sure: 8 },
      { id: 'b3l4', baslik: 'Yarı İletkenler', alt: 'Diyot · Transistör temelleri', sure: 7 },
      { id: 'b3q',  baslik: 'Bölüm Sınavı', alt: '20 soru · geçme 65%', sure: 10, sinav: true },
    ],
  },
  {
    id: 'b4', no: 4, kod: 'FREK', baslik: 'Frekans ve Bantlar',
    aciklama: 'HF / VHF / UHF bantları, dalga boyu, Türkiye bandplanı.',
    glyph: '14.205', renk: '#0369A1',
    dersler: [
      { id: 'b4l1', baslik: 'Frekans & Dalga Boyu', alt: 'kHz · MHz · λ = c / f', sure: 7 },
      { id: 'b4l2', baslik: 'HF Bantları', alt: '80m · 40m · 20m · 15m · 10m', sure: 8 },
      { id: 'b4l3', baslik: 'VHF / UHF & Yayılım', alt: '2m · 70cm · iyonosfer', sure: 8 },
      { id: 'b4l4', baslik: 'Türkiye Bandplanı', alt: 'TRAC frekans tahsisleri', sure: 7 },
      { id: 'b4q',  baslik: 'Bölüm Sınavı', alt: '20 soru · geçme 65%', sure: 10, sinav: true },
    ],
  },
  {
    id: 'b5', no: 5, kod: 'PROSEDÜR', baslik: 'Telsiz Prosedürleri',
    aciklama: 'Konuşma prosedürleri, çağrı işaretleri, acil durum.',
    glyph: 'OVER', renk: '#15803D',
    dersler: [
      { id: 'b5l1', baslik: 'Temel İfadeler', alt: 'Over · Out · Roger · Copy · Wilco · Break', sure: 6 },
      { id: 'b5l2', baslik: 'Çağrı İşaretleri', alt: 'TA · TB · TC prefix sistemi', sure: 7 },
      { id: 'b5l3', baslik: 'CQ ve QSO', alt: 'Bağlantı kurma · konuşma yapısı', sure: 7 },
      { id: 'b5l4', baslik: 'Acil Durum', alt: 'MAYDAY · Pan-Pan · log tutma', sure: 8 },
      { id: 'b5q',  baslik: 'Bölüm Sınavı', alt: '15 soru · geçme 60%', sure: 8, sinav: true },
    ],
  },
  {
    id: 'b6', no: 6, kod: 'TRAC', baslik: 'TRAC Sınav Hazırlık',
    aciklama: 'Mevzuat, lisans sınıfları, çıkmış sorular.',
    glyph: 'TA1', renk: '#7C2D12',
    dersler: [
      { id: 'b6l1', baslik: 'Mevzuat', alt: 'Telsiz kanunu · A / B / C lisans sınıfları', sure: 8 },
      { id: 'b6l2', baslik: 'Çıkmış Sorular — Set 1', alt: 'TRAC arşivi · 20 soru', sure: 10 },
      { id: 'b6l3', baslik: 'Çıkmış Sorular — Set 2', alt: 'TRAC arşivi · 20 soru', sure: 10 },
      { id: 'b6l4', baslik: 'Zayıf Nokta Tekrarı', alt: 'Spaced repetition', sure: 8 },
      { id: 'b6q',  baslik: 'Tam Deneme Sınavı', alt: '40 soru · gerçek format', sure: 25, sinav: true },
    ],
  },
];

window.ROZETLER = [
  { id: 'ilk_adim', isim: 'İlk Adım', glyph: '01', aciklama: 'İlk dersini tamamla', kazanildi: true },
  { id: 'nato_caylagi', isim: 'NATO Çaylağı', glyph: 'A·G', aciklama: "A'dan G'ye dersini bitir", kazanildi: true },
  { id: 'nato_ustasi', isim: 'NATO Ustası', glyph: 'A·Z', aciklama: 'NATO bölümünü geç', kazanildi: false },
  { id: '7_seri', isim: '7 Gün Seri', glyph: '07', aciklama: '7 gün üst üste çalış', kazanildi: true },
  { id: '30_seri', isim: '30 Gün Seri', glyph: '30', aciklama: '30 gün üst üste çalış', kazanildi: false },
  { id: 'mukemmel', isim: 'Mükemmeliyetçi', glyph: '★', aciklama: '5 ders üst üste hatasız', kazanildi: false },
  { id: 'q_bilgesi', isim: 'Q Kodu Bilgesi', glyph: 'QSL', aciklama: 'Tüm Q kodlarını öğren', kazanildi: false },
  { id: 'frekans', isim: 'Frekans Avcısı', glyph: '14M', aciklama: 'Frekans bölümünü tamamla', kazanildi: false },
  { id: 'trac', isim: 'TRAC Adayı', glyph: 'TA', aciklama: 'Tüm bölümleri tamamla', kazanildi: false },
];

// NATO alphabet for library / lesson preview
window.NATO = [
  ['A','Alpha','AL-FAH'],['B','Bravo','BRAH-VOH'],['C','Charlie','CHAR-LEE'],
  ['D','Delta','DELL-TAH'],['E','Echo','ECK-OH'],['F','Foxtrot','FOKS-TROT'],
  ['G','Golf','GOLF'],['H','Hotel','HOH-TEL'],['I','India','IN-DEE-AH'],
  ['J','Juliet','JEW-LEE-ETT'],['K','Kilo','KEY-LOH'],['L','Lima','LEE-MAH'],
  ['M','Mike','MIKE'],['N','November','NO-VEM-BER'],['O','Oscar','OSS-CAH'],
  ['P','Papa','PAH-PAH'],['Q','Quebec','KEH-BECK'],['R','Romeo','ROW-ME-OH'],
  ['S','Sierra','SEE-AIR-RAH'],['T','Tango','TANG-GO'],['U','Uniform','YOU-NEE-FORM'],
  ['V','Victor','VIK-TAH'],['W','Whiskey','WISS-KEY'],['X','X-ray','ECKS-RAY'],
  ['Y','Yankee','YANG-KEY'],['Z','Zulu','ZOO-LOO'],
];

// Q codes for library
window.QKODES = [
  ['QRZ', 'Kim çağırıyor?'],
  ['QSL', 'Anlaşıldı / onay'],
  ['QSO', 'Bağlantı / görüşme'],
  ['QTH', 'Konumum / konumun'],
  ['QRM', 'İnsan kaynaklı parazit'],
  ['QRN', 'Doğal parazit / gürültü'],
  ['QRP', 'Düşük güç'],
  ['QRT', 'Yayını durduruyorum'],
  ['QSB', 'Sinyal şiddeti değişiyor'],
  ['QSY', 'Frekans değiştir'],
];

// Morse code daily tip
window.MORSE = {
  'SOS':  '··· ——— ···',
  'CQ':   '—·—· ——·—',
  'TA':   '— ·—',
  'DE':   '—·· ·',
  '73':   '——··· ···——',
  'OK':   '——— —·—',
};

// Günün bilgisi pool
window.GUNUN_BILGISI = [
  { kod: 'QSY', metin: 'QSY = "Frekans değiştir". QSO ortasında uzlaşılan yeni frekansa geçişte kullanılır.' },
  { kod: '73',  metin: '73 = "İyi şanslar / selamlar". QSO sonunda kullanılan en yaygın kapanış kodudur.' },
  { kod: 'CQ',  metin: 'CQ = "Genel çağrı". Cevap verebilecek herhangi bir istasyona yapılan açık davet.' },
  { kod: 'SOS', metin: 'SOS Morse: ··· ——— ··· — Üç kısa, üç uzun, üç kısa; aralıksız okunur.' },
];

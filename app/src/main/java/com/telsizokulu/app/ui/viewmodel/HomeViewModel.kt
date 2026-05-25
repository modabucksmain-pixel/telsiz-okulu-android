package com.telsizokulu.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.data.model.Curriculum
import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.data.repository.CurriculumRepository
import com.telsizokulu.app.data.repository.ProgressRepository
import com.telsizokulu.app.engine.GamificationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class NextLesson(
    val bolumId: String,
    val altBolumId: String,
    val dersNo: Int,
    val baslik: String,
)

data class HomeUiState(
    val curriculum: Curriculum = Curriculum(),
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val bolumIlerlemeleri: Map<String, Int> = emptyMap(),
    val yukleniyor: Boolean = true,
    val selectedChapterIdx: Int = 0,
    val nextLesson: NextLesson? = null,
    val zayifKonuSayisi: Int = 0,
    val seviye: Int = 1,
    val seviyePct: Float = 0f,
    val onboardingGoster: Boolean = false,
)

class HomeViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepo.updateStreak()
            val onboardingGorebilir = !progressRepo.getOnboardingGoruldu()
            if (onboardingGorebilir) {
                _uiState.value = _uiState.value.copy(onboardingGoster = true)
            }
            val curriculum = curriculumRepo.loadCurriculum()
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                val bolumIlerlemeleri = curriculum.bolumler.associate { bolum ->
                    bolum.id to GamificationEngine.getBolumIlerlemeYuzdesi(ilerleme, bolum.id)
                }
                val (seviye, seviyePct) = computeSeviye(ilerleme.kullanici.xp)
                val currentIdx = _uiState.value.selectedChapterIdx
                val nextLesson = findNextLesson(curriculum, ilerleme, currentIdx)
                val zayif = ilerleme.kartlar.count { (_, k) -> k.yanlis > 0 && k.durum != "ogrendim" }

                _uiState.value = _uiState.value.copy(
                    curriculum       = curriculum,
                    ilerleme         = ilerleme,
                    bolumIlerlemeleri= bolumIlerlemeleri,
                    yukleniyor       = false,
                    nextLesson       = nextLesson,
                    zayifKonuSayisi  = zayif,
                    seviye           = seviye,
                    seviyePct        = seviyePct,
                )
            }
        }
    }

    fun onboardingKapat() {
        _uiState.value = _uiState.value.copy(onboardingGoster = false)
        viewModelScope.launch { progressRepo.setOnboardingGoruldu() }
    }

    fun selectChapter(idx: Int) {
        val curriculum = _uiState.value.curriculum
        val ilerleme   = _uiState.value.ilerleme
        val nextLesson = findNextLesson(curriculum, ilerleme, idx)
        _uiState.value = _uiState.value.copy(
            selectedChapterIdx = idx,
            nextLesson         = nextLesson,
        )
    }

    private fun findNextLesson(
        curriculum: Curriculum,
        ilerleme: KullaniciIlerleme,
        chapterIdx: Int,
    ): NextLesson? {
        val bolum = curriculum.bolumler.getOrNull(chapterIdx) ?: return null
        for (altBolum in bolum.altBolumler) {
            val altBolumIlerleme = ilerleme.bolumler[bolum.id]?.altBolumler?.get(altBolum.id)
            // Kilitli alt bölüme atlama — sınav geçilmeden sonraki alt bölüme geçilemez.
            if ((altBolumIlerleme?.durum ?: "kilitli") == "kilitli") continue
            val tamamlananDersler = altBolumIlerleme?.tamamlananDersler ?: emptyList()
            val maxTamamlanan = tamamlananDersler.maxOrNull() ?: 0
            for (ders in altBolum.dersler) {
                val done = ders.no in tamamlananDersler
                // Yalnızca erişilebilir dersi öner (bir sonraki sıradaki).
                if (!done && ders.no <= maxTamamlanan + 1) {
                    return NextLesson(bolum.id, altBolum.id, ders.no, ders.baslik)
                }
            }
        }
        return null
    }

    private fun computeSeviye(xp: Int): Pair<Int, Float> {
        val thresholds = listOf(0, 200, 500, 1000, 2000, 3500, 5000)
        var level = 1
        for (i in thresholds.indices.reversed()) {
            if (xp >= thresholds[i]) { level = i + 1; break }
        }
        level = level.coerceIn(1, 7)
        val low  = thresholds[level - 1]
        val high = if (level < thresholds.size) thresholds[level] else thresholds.last()
        val pct  = if (high == low) 1f else (xp - low).toFloat() / (high - low).toFloat()
        return level to pct.coerceIn(0f, 1f)
    }
}

class HomeViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(curriculumRepo, progressRepo) as T
}

// ── BolumViewModel ─────────────────────────────────────────────────────────

data class BolumUiState(
    val bolum: Bolum? = null,
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val yukleniyor: Boolean = true,
)

class BolumViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val bolumId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolumUiState())
    val uiState: StateFlow<BolumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val curriculum = curriculumRepo.loadCurriculum()
            val bolum      = curriculum.bolumler.find { it.id == bolumId }
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                _uiState.value = BolumUiState(bolum = bolum, ilerleme = ilerleme, yukleniyor = false)
            }
        }
    }
}

class BolumViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val bolumId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BolumViewModel(curriculumRepo, progressRepo, bolumId) as T
}

// ── ProfileViewModel ───────────────────────────────────────────────────────

data class ProfileUiState(
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val rozetTanimlari: List<com.telsizokulu.app.data.model.RozetTanim> = emptyList(),
    val curriculum: Curriculum = Curriculum(),
    val yukleniyor: Boolean = true,
)

class ProfileViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val curriculum   = curriculumRepo.loadCurriculum()
            val rozetDosyasi = curriculumRepo.loadRozetler()
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                _uiState.value = ProfileUiState(
                    ilerleme      = ilerleme,
                    rozetTanimlari= rozetDosyasi.rozetler,
                    curriculum    = curriculum,
                    yukleniyor    = false,
                )
            }
        }
    }

    fun veriSifirla() { viewModelScope.launch { progressRepo.sifirla() } }

    fun exportJson(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(progressRepo.exportJson()) }
    }

    fun importJson(json: String) { viewModelScope.launch { progressRepo.importJson(json) } }
}

class ProfileViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(curriculumRepo, progressRepo) as T
}

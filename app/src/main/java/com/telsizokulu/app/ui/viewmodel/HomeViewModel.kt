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

data class HomeUiState(
    val curriculum: Curriculum = Curriculum(),
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val bolumIlerlemeleri: Map<String, Int> = emptyMap(),
    val yukleniyor: Boolean = true
)

class HomeViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepo.updateStreak()
            val curriculum = curriculumRepo.loadCurriculum()
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                val bolumIlerlemeleri = curriculum.bolumler.associate { bolum ->
                    bolum.id to GamificationEngine.getBolumIlerlemeYuzdesi(ilerleme, bolum.id)
                }
                _uiState.value = HomeUiState(
                    curriculum = curriculum,
                    ilerleme = ilerleme,
                    bolumIlerlemeleri = bolumIlerlemeleri,
                    yukleniyor = false
                )
            }
        }
    }
}

class HomeViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(curriculumRepo, progressRepo) as T
    }
}

// ── BolumViewModel ─────────────────────────────────────────────────

data class BolumUiState(
    val bolum: Bolum? = null,
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val yukleniyor: Boolean = true
)

class BolumViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val bolumId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolumUiState())
    val uiState: StateFlow<BolumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val curriculum = curriculumRepo.loadCurriculum()
            val bolum = curriculum.bolumler.find { it.id == bolumId }
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                _uiState.value = BolumUiState(bolum = bolum, ilerleme = ilerleme, yukleniyor = false)
            }
        }
    }
}

class BolumViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val bolumId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BolumViewModel(curriculumRepo, progressRepo, bolumId) as T
    }
}

// ── ProfileViewModel ───────────────────────────────────────────────

data class ProfileUiState(
    val ilerleme: KullaniciIlerleme = KullaniciIlerleme(),
    val rozetTanimlari: List<com.telsizokulu.app.data.model.RozetTanim> = emptyList(),
    val curriculum: Curriculum = Curriculum(),
    val yukleniyor: Boolean = true
)

class ProfileViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val curriculum = curriculumRepo.loadCurriculum()
            val rozetDosyasi = curriculumRepo.loadRozetler()
            progressRepo.ilerlemFlow.collectLatest { ilerleme ->
                _uiState.value = ProfileUiState(
                    ilerleme = ilerleme,
                    rozetTanimlari = rozetDosyasi.rozetler,
                    curriculum = curriculum,
                    yukleniyor = false
                )
            }
        }
    }

    fun veriSifirla() {
        viewModelScope.launch { progressRepo.sifirla() }
    }

    fun exportJson(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(progressRepo.exportJson()) }
    }

    fun importJson(json: String) {
        viewModelScope.launch { progressRepo.importJson(json) }
    }
}

class ProfileViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(curriculumRepo, progressRepo) as T
    }
}

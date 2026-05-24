package com.telsizokulu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.BodySmall
import com.telsizokulu.app.ui.theme.ChapterName
import com.telsizokulu.app.ui.theme.Danger
import com.telsizokulu.app.ui.theme.EyebrowMono
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.SansFamily
import com.telsizokulu.app.ui.theme.SpektrumStreak
import com.telsizokulu.app.ui.theme.Success

data class GrillQuestion(
    val prompt: String,
    val subtitle: String? = null,
    val options: List<String>,
    val correctIndex: Int,
)

@Composable
fun GrillMeFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = SpektrumStreak,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(52.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
            Text("⚡", fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "DRILL",
            style = MutedMono.copy(fontSize = 9.sp, letterSpacing = 0.6.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrillMeBottomSheet(
    bolumId: String,
    bolumKod: String,
    bolumRenk: Color,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val question   = remember(bolumId) { generateGrillQuestion(bolumId) }
    var picked     by remember { mutableStateOf<Int?>(null) }
    var reveal     by remember { mutableStateOf(false) }
    val isCorrect  = picked == question.correctIndex

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
        ) {
            // Eyebrow + close
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⚡ GRILL ME · $bolumKod",
                    style = EyebrowMono.copy(color = SpektrumStreak),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Question
            Text(
                text = question.prompt,
                style = ChapterName.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 23.sp,
            )

            // Subtitle (big monospace card)
            question.subtitle?.let { sub ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sub,
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 2.sp,
                        color = bolumRenk,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Options
            question.options.forEachIndexed { i, opt ->
                val isPicked = picked == i
                val showOk   = reveal && i == question.correctIndex
                val showBad  = reveal && isPicked && !isCorrect
                val optBg    = when {
                    showOk  -> Success.copy(alpha = 0.078f)
                    showBad -> Danger.copy(alpha = 0.078f)
                    isPicked -> bolumRenk.copy(alpha = 0.12f)
                    else    -> MaterialTheme.colorScheme.surfaceVariant
                }
                val optBorder = when {
                    showOk  -> Success
                    showBad -> Danger
                    isPicked -> bolumRenk
                    else    -> MaterialTheme.colorScheme.outlineVariant
                }
                val letterColor = when {
                    showOk  -> Success
                    showBad -> Danger
                    isPicked -> bolumRenk
                    else    -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(optBg)
                        .border(1.5.dp, optBorder, RoundedCornerShape(10.dp))
                        .then(if (!reveal) Modifier.clickable { picked = i } else Modifier)
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ('A' + i).toString(),
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = letterColor,
                        modifier = Modifier.width(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = opt,
                        fontFamily = SansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (showOk)  Text("✓", color = Success, fontWeight = FontWeight.Bold)
                    if (showBad) Text("✕", color = Danger,  fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Feedback
            if (reveal) {
                Text(
                    text = if (isCorrect) "✓ Doğru! Harika."
                           else "✕ Doğru cevap: ${question.options[question.correctIndex]}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isCorrect) Success else Danger,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            // CTA button
            Button(
                onClick = { if (!reveal) reveal = true else onDismiss() },
                enabled = picked != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor   = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.outline,
                    disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(11.dp),
            ) {
                Text(
                    text = if (reveal) "Kapat" else "Kontrol et",
                    fontFamily = SansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

fun generateGrillQuestion(bolumId: String): GrillQuestion {
    val natoList = listOf(
        "A" to "Alfa",    "B" to "Bravo",   "C" to "Charlie", "D" to "Delta",
        "E" to "Echo",    "F" to "Foxtrot", "G" to "Golf",    "H" to "Hotel",
        "I" to "India",   "J" to "Juliet",  "K" to "Kilo",    "L" to "Lima",
        "M" to "Mike",    "N" to "November","O" to "Oscar",   "P" to "Papa",
        "Q" to "Quebec",  "R" to "Romeo",   "S" to "Sierra",  "T" to "Tango",
        "U" to "Uniform", "V" to "Victor",  "W" to "Whiskey", "X" to "X-ray",
        "Y" to "Yankee",  "Z" to "Zulu",
    )
    val qList = listOf(
        "QRZ" to "Kim arıyor?", "QSL" to "Alındı / Anlaşıldı",
        "QSO" to "İletişim kuruldu", "QTH" to "Konum nerede?",
        "QRM" to "İnsan kaynaklı parazit", "QRN" to "Doğal parazit",
        "QRP" to "Gücü azalt", "QRO" to "Gücü artır",
        "QSY" to "Frekans değiştir", "QRX" to "Bekle",
    )
    val pools = mapOf(
        "bolum_3" to listOf(
            GrillQuestion("Ohm kanunu formülü?", "V = ?", listOf("V = I·R","V = I/R","P = V·I","R = V+I"), 0),
            GrillQuestion("Güç formülü?", null, listOf("P = V·I","P = V/I","P = I/V","P = V+I"), 0),
            GrillQuestion("12V / 4Ω = ?", "V/R", listOf("2A","3A","4A","6A"), 1),
        ),
        "bolum_4" to listOf(
            GrillQuestion("20m bandı kaç MHz?", "20m", listOf("7","14","21","28"), 1),
            GrillQuestion("2m bandı kaç MHz?", "2m", listOf("50","144","435","1296"), 1),
            GrillQuestion("Dalga boyu formülü?", "λ = ?", listOf("λ=c/f","λ=c·f","λ=f/c","λ=c²/f"), 0),
        ),
        "bolum_5" to listOf(
            GrillQuestion("\"Anlaşıldı\" kısaltması?", null, listOf("Over","Roger","Wilco","Break"), 1),
            GrillQuestion("Türkiye ön eki?", null, listOf("DL","F","TA","G"), 2),
            GrillQuestion("Acil durum çağrısı?", null, listOf("CQ","QRZ","MAYDAY","73"), 2),
        ),
        "bolum_6" to listOf(
            GrillQuestion("TRAC neyin kısaltması?", null, listOf("Türkiye Radyo Amatörleri Cemiyeti","Telsiz Radyo A.C.","Teknik Radyo A.C.","T.R.A.C."), 0),
            GrillQuestion("Sınav geçme eşiği?", null, listOf("50%","60%","70%","80%"), 1),
        ),
    )

    return when (bolumId) {
        "bolum_1" -> {
            val shuffled = natoList.shuffled()
            val correct  = shuffled.first()
            val opts     = (shuffled.drop(1).take(3).map { it.second } + correct.second).shuffled()
            GrillQuestion(
                prompt       = "\"${correct.first}\" harfinin NATO karşılığı?",
                subtitle     = correct.first,
                options      = opts,
                correctIndex = opts.indexOf(correct.second),
            )
        }
        "bolum_2" -> {
            val shuffled = qList.shuffled()
            val correct  = shuffled.first()
            val opts     = (shuffled.drop(1).take(3).map { it.second } + correct.second).shuffled()
            GrillQuestion(
                prompt       = "${correct.first} ne anlama gelir?",
                subtitle     = correct.first,
                options      = opts,
                correctIndex = opts.indexOf(correct.second),
            )
        }
        else -> {
            val pool = pools[bolumId] ?: pools["bolum_5"]!!
            pool.random()
        }
    }
}

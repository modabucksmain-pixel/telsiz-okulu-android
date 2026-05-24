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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.LessonTitle
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.SpektrumStreak
import com.telsizokulu.app.ui.theme.SpektrumStreakTint

enum class LessonStatus { LOCKED, ACCESSIBLE, DONE }

@Composable
fun HomeLessonRow(
    no: Int,
    baslik: String,
    isSinav: Boolean,
    status: LessonStatus,
    bolumRenk: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accessible = status != LessonStatus.LOCKED
    val done = status == LessonStatus.DONE
    val leftBorder = when {
        done       -> bolumRenk
        accessible -> bolumRenk.copy(alpha = 0.53f)
        else       -> MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 7.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .then(
                if (accessible) Modifier.clickable(onClick = onClick)
                else Modifier.alpha(0.5f)
            )
            .padding(start = 0.dp)
            .then(Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(54.dp)
                .background(leftBorder, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
        )

        Spacer(Modifier.width(10.dp))

        // number / status badge
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(if (isSinav) RoundedCornerShape(7.dp) else RoundedCornerShape(17.dp))
                .background(
                    when {
                        done       -> bolumRenk
                        accessible -> bolumRenk.copy(alpha = 0.086f)
                        else       -> MaterialTheme.colorScheme.outlineVariant
                    }
                )
                .border(
                    1.5.dp,
                    when {
                        done       -> bolumRenk
                        accessible -> bolumRenk.copy(alpha = 0.267f)
                        else       -> MaterialTheme.colorScheme.outlineVariant
                    },
                    if (isSinav) RoundedCornerShape(7.dp) else RoundedCornerShape(17.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    !accessible -> "🔒"
                    done        -> "✓"
                    isSinav     -> "Q"
                    else        -> no.toString().padStart(2, '0')
                },
                fontFamily = MonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = when {
                    done       -> Color.White
                    accessible -> bolumRenk
                    else       -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(vertical = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = baslik,
                    style = LessonTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (isSinav) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SINAV",
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = SpektrumStreak,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SpektrumStreakTint)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))
    }
}

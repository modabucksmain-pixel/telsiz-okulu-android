package com.telsizokulu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.Danger
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.SansFamily
import com.telsizokulu.app.ui.theme.Success

@Composable
fun OptionButton(
    index: Int,
    text: String,
    bolumRenk: Color,
    state: OptionState,
    onClick: () -> Unit,
) {
    val optBg = when (state) {
        OptionState.CORRECT -> Success.copy(alpha = 0.078f)
        OptionState.WRONG -> Danger.copy(alpha = 0.078f)
        OptionState.SELECTED -> bolumRenk.copy(alpha = 0.12f)
        OptionState.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
    }
    val optBorder = when (state) {
        OptionState.CORRECT -> Success
        OptionState.WRONG -> Danger
        OptionState.SELECTED -> bolumRenk
        OptionState.DEFAULT -> MaterialTheme.colorScheme.outlineVariant
    }
    val letterColor = when (state) {
        OptionState.CORRECT -> Success
        OptionState.WRONG -> Danger
        OptionState.SELECTED -> bolumRenk
        OptionState.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 7.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(optBg)
            .border(1.5.dp, optBorder, RoundedCornerShape(10.dp))
            .clickable(enabled = state == OptionState.DEFAULT || state == OptionState.SELECTED, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ('A' + index).toString(),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = letterColor,
            modifier = Modifier.width(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = SansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (state == OptionState.CORRECT) Text("✓", color = Success, fontWeight = FontWeight.Bold)
        if (state == OptionState.WRONG) Text("✕", color = Danger, fontWeight = FontWeight.Bold)
    }
}

enum class OptionState {
    DEFAULT, SELECTED, CORRECT, WRONG
}

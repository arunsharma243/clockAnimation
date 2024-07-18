package com.example.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RunningClock(
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    // Update the time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000L)
        }
    }

    val hours = currentTime.get(Calendar.HOUR).toFloat()
    val minutes = currentTime.get(Calendar.MINUTE).toFloat()
//    val seconds = currentTime.get(Calendar.SECOND).toFloat()

    Canvas(modifier = Modifier.size(220.dp)) {
        val radius = size.minDimension / 2
        val center = Offset(x = size.width / 2, y = size.height / 2)
        val start=Offset(x=size.width/2,y=size.height/2)
        // Draw the clock circle
        drawCircle(
            color = Color.Black,
            center = center,
            radius = radius,
            //style = Stroke(width = 1.dp.toPx())
        )

        // Draw hour hand
        val hourAngle = (hours + minutes / 60) * (360 / 12) - 90
        val hourHandLength = radius * 0.5
        val hourHandEnd = Offset(
            x = center.x + (hourHandLength * cos(hourAngle * PI / 180)).toFloat(),
            y = center.y + (hourHandLength * sin(hourAngle * PI / 180)).toFloat()
        )
        drawLine(
            color = Color.Red,
            start = center,
            end = hourHandEnd,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw minute hand
        val minuteAngle = minutes * (360 / 60) - 90
        val minuteHandLength = radius * 0.8
        val minuteHandEnd = Offset(
            x = center.x + (minuteHandLength * cos(minuteAngle * PI / 180)).toFloat(),
            y = center.y + (minuteHandLength * sin(minuteAngle * PI / 180)).toFloat()
        )
        drawLine(
            color = Color.Blue,
            start = center,
            end = minuteHandEnd,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

    }
}

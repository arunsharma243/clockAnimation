package com.example.clock


import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.core.view.WindowCompat
import com.example.clock.ui.theme.ClockTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHADER_SRC="""
    uniform float2 size;
    uniform float time;
    uniform shader composable;
    
    const float FREQ_RANGE = 64.0;
const float PI = 3.1415;
const float RADIUS = 0.25;
const float BRIGHTNESS = 0.2;
const float SPEED = 0.2;

// Convert HSV to RGB
float3 hsv2rgb(float3 c) {
    float4 K = float4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float luma(float3 color) {
    return dot(color, float3(0.299, 0.587, 0.114));
}

float getfrequency(float x) {
    return composable.eval( float2(floor(x * FREQ_RANGE + 1.0) / FREQ_RANGE, 0.0)).x + 0.06;
}

float getfrequency_smooth(float x) {
    float index = floor(x * FREQ_RANGE) / FREQ_RANGE;
    float next = floor(x * FREQ_RANGE + 1.0) / FREQ_RANGE;
    return mix(getfrequency(index), getfrequency(next), smoothstep(0.0, 1.0, fract(x * FREQ_RANGE)));
}

float getfrequency_blend(float x) {
    return mix(getfrequency(x), getfrequency_smooth(x), 0.5);
}

float3 doHalo(float2 fragment, float radius) {
    float dist = length(fragment);
    float ring = 1.0 / (abs(dist - radius) + 0.005);

    float b = dist < radius ? BRIGHTNESS * 0.6 : BRIGHTNESS;

    float3 col = float3(0.0);

    float angle = atan(fragment.x, fragment.y);
    col += hsv2rgb(float3((angle + time * 0.25) / (PI * 2.0), 0.6, 0.5)) * ring * b;

    float frequency = max(getfrequency(abs(angle / PI)) - 0.02, 0.0);
    col *= frequency * 0.5;

    // Black halo
    col *= smoothstep(radius * 0.86, radius, dist);

    return col;
}

float3 doLine(float2 fragment, float radius, float x) {
    float3 col = hsv2rgb(float3(x * 0.23 + time * 0.12, 1.0, 1.0));

    float freq = abs(fragment.x * 0.5);

    col *= (1.0 / abs(fragment.y)) * BRIGHTNESS * getfrequency(freq);    
    col = col * smoothstep(radius, radius * 1.8, abs(fragment.x));

    return col;
}

half4 main(float2 fragCoord) {
    float2 fragPos = fragCoord / size.xy;
    fragPos = (fragPos - 0.5) * 2.0;
    fragPos.x *= size.x / size.y;

    float3 color = float3(0.0134, 0.052, 0.1);
    color += doHalo(fragPos, RADIUS);

    float c = cos(time * SPEED);
    float s = sin(time * SPEED);
    float2 rot = float2x2(c, s, -s, c) * fragPos;
    // color += doLine(rot, RADIUS, rot.x);

    color += max(luma(color) - 1.0, 0.0);

    return half4(color, 1.0);
}
"""


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)

        val shader= RuntimeShader(SHADER_SRC)
        val photo=BitmapFactory.decodeResource(resources,R.drawable.page3)

        setContent {
            val scope= rememberCoroutineScope()
            val timeMs=remember{ mutableStateOf(0f) }
            LaunchedEffect(Unit ){
                scope.launch{
                    while(true){
                        timeMs.value = (System.currentTimeMillis() % 100_000L) / 1_000f
                    delay(10)
                    }
                }
            }
            ClockTheme {
                // A surface container using the 'background' color from the theme
                Box(
                  modifier = Modifier,
                     contentAlignment=Alignment.Center,
                   // color = MaterialTheme.colorScheme.background
                ) {
                   Image(bitmap=photo.asImageBitmap(),
                            modifier= Modifier
                                .onSizeChanged { size ->
                                    shader.setFloatUniform(
                                        "size",
                                        size.width.toFloat(),
                                        size.height.toFloat()
                                    )
                                }
                                .graphicsLayer {
                                    clip = true
                                    shader.setFloatUniform("time", timeMs.value)
                                    renderEffect = RenderEffect
                                        .createRuntimeShaderEffect(shader, "composable")
                                        .asComposeRenderEffect()
                                },
                       contentScale = ContentScale.FillHeight,
                       contentDescription = null,
                       )
                    RunningClock()
                    }
                }
            }
        }
    }






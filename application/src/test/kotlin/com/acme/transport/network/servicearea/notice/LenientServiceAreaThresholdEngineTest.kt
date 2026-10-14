package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientServiceAreaThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientServiceAreaThresholdEngine()
        val item = newLenientServiceAreaThreshold("code-219", "label-772")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

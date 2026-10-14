package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEvChargeThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEvChargeThresholdEngine()
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

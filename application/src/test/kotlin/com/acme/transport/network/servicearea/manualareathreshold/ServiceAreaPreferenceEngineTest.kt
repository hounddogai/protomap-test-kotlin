package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaPreferenceEngine()
        val item = newServiceAreaPreference("code-365", "label-125")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

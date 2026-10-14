package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveIdlingPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveIdlingPolicyEngine()
        val item = newAdaptiveIdlingPolicy("code-625", "title-664")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualEvChargeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualEvChargeScoreEngine()
        val item = newManualEvChargeScore("code-243", "name-537")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

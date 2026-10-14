package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTireBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTireBaselineEngine()
        val item = newManualTireBaseline("code-533", "title-608")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

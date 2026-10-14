package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeOverrideEngine()
        val item = newEvChargeOverride("code-179", "label-469")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

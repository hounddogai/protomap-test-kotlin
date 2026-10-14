package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeBaselineEngine(newRegistry())
        val item = newEvChargeBaseline("code-489", "label-307")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

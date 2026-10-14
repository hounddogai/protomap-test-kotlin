package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeBaselineEngine(newRegistry())
        val item = newStrictEvChargeBaseline("code-847", "title-380")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

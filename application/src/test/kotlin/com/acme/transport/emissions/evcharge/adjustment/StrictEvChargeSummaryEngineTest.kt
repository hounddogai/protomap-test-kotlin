package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeSummaryEngine(newRegistry())
        val item = newStrictEvChargeSummary("code-47", "label-781")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeSummaryEngine(newRegistry())
        val item = newEvChargeSummary("code-288", "title-497")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

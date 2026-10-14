package com.acme.transport.network.transittime.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeRollupEngine()
        val item = newTransitTimeRollup("code-359", "label-924")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

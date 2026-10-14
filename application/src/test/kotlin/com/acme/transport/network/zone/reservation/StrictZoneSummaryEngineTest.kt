package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictZoneSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictZoneSummaryEngine(newRegistry())
        val item = newStrictZoneSummary("code-47", "label-44")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTransitTimeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTransitTimeSummaryEngine(newRegistry())
        val item = newScheduledTransitTimeSummary("code-453", "name-292")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

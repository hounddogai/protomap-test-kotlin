package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterSummaryEngine()
        val item = newRosterSummary("code-464", "label-279")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

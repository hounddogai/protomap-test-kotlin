package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRosterEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRosterEntryEngine()
        val item = newRollingRosterEntry("code-17", "label-175")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

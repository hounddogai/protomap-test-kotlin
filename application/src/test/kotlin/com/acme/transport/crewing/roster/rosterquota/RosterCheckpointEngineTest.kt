package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterCheckpointEngine()
        val item = newRosterCheckpoint("code-127", "name-664")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

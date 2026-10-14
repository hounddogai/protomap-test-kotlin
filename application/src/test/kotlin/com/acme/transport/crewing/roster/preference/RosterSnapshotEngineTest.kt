package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterSnapshotEngine(newRegistry())
        val item = newRosterSnapshot("code-434", "name-755")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

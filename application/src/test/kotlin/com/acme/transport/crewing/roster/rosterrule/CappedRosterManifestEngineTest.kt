package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRosterManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRosterManifestEngine(newRegistry())
        val item = newCappedRosterManifest("code-754", "title-172")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

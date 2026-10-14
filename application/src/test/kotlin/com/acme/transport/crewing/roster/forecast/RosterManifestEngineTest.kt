package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterManifestEngine()
        val item = newRosterManifest("code-235", "title-754")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

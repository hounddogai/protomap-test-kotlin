package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterDigestEngine()
        val item = newRosterDigest("code-333", "name-649")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

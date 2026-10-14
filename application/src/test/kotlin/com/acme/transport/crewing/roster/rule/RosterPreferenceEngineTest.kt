package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterPreferenceEngine()
        val item = newRosterPreference("code-689", "title-829")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

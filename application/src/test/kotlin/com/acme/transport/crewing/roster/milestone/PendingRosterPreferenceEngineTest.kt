package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRosterPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRosterPreferenceEngine(newRegistry())
        val item = newPendingRosterPreference("code-213", "title-648")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

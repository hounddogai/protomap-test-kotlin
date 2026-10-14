package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRestBreakDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRestBreakDraftEngine()
        val item = newStrictRestBreakDraft("code-287", "name-53")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

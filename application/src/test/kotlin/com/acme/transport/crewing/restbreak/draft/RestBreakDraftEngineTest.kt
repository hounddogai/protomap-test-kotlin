package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakDraftEngine()
        val item = newRestBreakDraft("code-778", "name-364")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

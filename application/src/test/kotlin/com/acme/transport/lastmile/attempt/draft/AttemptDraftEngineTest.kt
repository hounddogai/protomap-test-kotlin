package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptDraftEngine()
        val item = newAttemptDraft("code-799", "title-569")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

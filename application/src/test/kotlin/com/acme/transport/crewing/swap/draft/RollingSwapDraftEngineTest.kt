package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapDraftEngine()
        val item = newRollingSwapDraft("code-406", "name-933")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledCutoffDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledCutoffDraftEngine()
        val item = newScheduledCutoffDraft("code-670", "label-29")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

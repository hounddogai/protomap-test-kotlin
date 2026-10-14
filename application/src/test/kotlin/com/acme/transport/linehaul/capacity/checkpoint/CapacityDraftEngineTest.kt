package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityDraftEngine()
        val item = newCapacityDraft("code-200", "title-753")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

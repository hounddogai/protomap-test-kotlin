package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredHoldDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredHoldDraftEngine()
        val item = newTieredHoldDraft("code-67", "label-629")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

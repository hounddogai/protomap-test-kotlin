package com.acme.transport.terminal.sortation.score

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackSortationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackSortationRecordEngine()
        val item = newFallbackSortationRecord("code-251", "label-854")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

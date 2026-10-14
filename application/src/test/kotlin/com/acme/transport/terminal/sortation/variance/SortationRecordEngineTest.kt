package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class SortationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationRecordEngine()
        val item = newSortationRecord("code-490", "label-354")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

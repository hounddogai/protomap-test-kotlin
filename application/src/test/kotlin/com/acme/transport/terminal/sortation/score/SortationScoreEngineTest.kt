package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationScoreEngine(newRegistry())
        val item = newSortationScore("code-121", "label-500")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

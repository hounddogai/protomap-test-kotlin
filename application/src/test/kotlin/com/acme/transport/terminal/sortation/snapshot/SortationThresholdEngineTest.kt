package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationThresholdEngine(newRegistry())
        val item = newSortationThreshold("code-814", "label-462")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

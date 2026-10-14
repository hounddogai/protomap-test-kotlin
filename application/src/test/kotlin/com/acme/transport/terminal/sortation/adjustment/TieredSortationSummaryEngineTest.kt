package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredSortationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredSortationSummaryEngine(newRegistry())
        val item = newTieredSortationSummary("code-26", "label-887")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

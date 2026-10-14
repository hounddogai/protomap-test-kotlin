package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationTierEngine(newRegistry())
        val item = newSortationTier("code-326", "label-991")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

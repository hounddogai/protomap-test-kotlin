package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationAllocationEngine(newRegistry())
        val item = newSortationAllocation("code-176", "name-204")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

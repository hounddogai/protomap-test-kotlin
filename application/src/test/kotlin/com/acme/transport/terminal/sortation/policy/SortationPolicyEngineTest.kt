package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationPolicyEngine(newRegistry())
        val item = newSortationPolicy("code-941", "name-758")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropRuleEngine(newRegistry())
        val item = newDropRule("code-210", "title-33")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

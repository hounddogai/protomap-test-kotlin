package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRestBreakRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRestBreakRuleEngine(newRegistry())
        val item = newRegionalRestBreakRule("code-587", "label-103")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationRuleEngine(newRegistry())
        val item = newUtilizationRule("code-718", "name-771")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

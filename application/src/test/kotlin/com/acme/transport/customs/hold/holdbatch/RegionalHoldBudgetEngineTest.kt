package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalHoldBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalHoldBudgetEngine(newRegistry())
        val item = newRegionalHoldBudget("code-217", "name-958")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

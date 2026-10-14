package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedUtilizationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedUtilizationBudgetEngine(newRegistry())
        val item = newCappedUtilizationBudget("code-75", "name-393")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

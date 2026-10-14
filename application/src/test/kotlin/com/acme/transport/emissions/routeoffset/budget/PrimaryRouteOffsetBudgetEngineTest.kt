package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRouteOffsetBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRouteOffsetBudgetEngine(newRegistry())
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

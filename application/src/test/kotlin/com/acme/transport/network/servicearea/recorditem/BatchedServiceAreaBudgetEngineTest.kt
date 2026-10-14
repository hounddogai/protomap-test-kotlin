package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedServiceAreaBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedServiceAreaBudgetEngine(newRegistry())
        val item = newBatchedServiceAreaBudget("code-973", "title-357")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

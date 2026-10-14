package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTenderBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTenderBudgetEngine(newRegistry())
        val item = newPendingTenderBudget("code-697", "title-871")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

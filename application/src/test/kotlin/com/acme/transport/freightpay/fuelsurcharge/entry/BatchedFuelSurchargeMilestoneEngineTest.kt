package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedFuelSurchargeMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedFuelSurchargeMilestoneEngine(newRegistry())
        val item = newBatchedFuelSurchargeMilestone("code-648", "name-534")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

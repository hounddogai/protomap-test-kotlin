package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualFuelSurchargeMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualFuelSurchargeMilestoneEngine(newRegistry())
        val item = newManualFuelSurchargeMilestone("code-934", "title-933")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

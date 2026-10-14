package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelSurchargeMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelSurchargeMilestoneEngine(newRegistry())
        val item = newRollingFuelSurchargeMilestone("code-816", "name-345")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

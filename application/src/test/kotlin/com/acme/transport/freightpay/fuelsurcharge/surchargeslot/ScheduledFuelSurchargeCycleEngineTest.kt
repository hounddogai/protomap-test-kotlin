package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledFuelSurchargeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledFuelSurchargeCycleEngine(newRegistry())
        val item = newScheduledFuelSurchargeCycle("code-31", "label-83")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

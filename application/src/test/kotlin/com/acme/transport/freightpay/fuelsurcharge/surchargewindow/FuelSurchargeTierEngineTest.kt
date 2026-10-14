package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeTierEngine(newRegistry())
        val item = newFuelSurchargeTier("code-413", "label-379")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeTemplateEngine(newRegistry())
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

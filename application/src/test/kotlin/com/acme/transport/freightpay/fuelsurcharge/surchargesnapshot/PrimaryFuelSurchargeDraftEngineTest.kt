package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryFuelSurchargeDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryFuelSurchargeDraftEngine()
        val item = newPrimaryFuelSurchargeDraft("code-636", "name-786")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

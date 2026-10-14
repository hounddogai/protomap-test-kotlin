package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictFuelSurchargeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictFuelSurchargeSummaryEngine()
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

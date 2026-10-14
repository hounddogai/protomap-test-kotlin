package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredFuelSurchargeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredFuelSurchargeSummaryEngine(newRegistry())
        val item = newTieredFuelSurchargeSummary("code-146", "title-273")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

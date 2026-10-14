package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelSurchargeThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelSurchargeThresholdEngine(newRegistry())
        val item = newGlobalFuelSurchargeThreshold("code-896", "title-42")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

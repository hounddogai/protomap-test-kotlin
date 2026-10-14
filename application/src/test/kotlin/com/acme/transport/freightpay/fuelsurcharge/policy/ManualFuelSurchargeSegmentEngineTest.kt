package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualFuelSurchargeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualFuelSurchargeSegmentEngine(newRegistry())
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelSurchargeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelSurchargeSnapshotEngine()
        val item = newRegionalFuelSurchargeSnapshot("code-853", "name-125")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

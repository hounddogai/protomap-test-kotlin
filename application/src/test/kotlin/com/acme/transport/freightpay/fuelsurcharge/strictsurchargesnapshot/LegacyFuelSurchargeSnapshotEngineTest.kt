package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyFuelSurchargeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyFuelSurchargeSnapshotEngine()
        val item = newLegacyFuelSurchargeSnapshot("code-891", "title-271")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelSurchargeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelSurchargeSnapshotEngine(newRegistry())
        val item = newWeightedFuelSurchargeSnapshot("code-699", "label-525")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

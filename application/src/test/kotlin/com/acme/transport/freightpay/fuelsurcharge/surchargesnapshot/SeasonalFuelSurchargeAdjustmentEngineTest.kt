package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFuelSurchargeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFuelSurchargeAdjustmentEngine(newRegistry())
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

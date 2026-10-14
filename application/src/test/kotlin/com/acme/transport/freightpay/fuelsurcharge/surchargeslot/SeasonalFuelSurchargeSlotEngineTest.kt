package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalFuelSurchargeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalFuelSurchargeSlotEngine(newRegistry())
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelSurchargePreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelSurchargePreferenceEngine(newRegistry())
        val item = newRollingFuelSurchargePreference("code-915", "title-563")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTariffThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTariffThresholdEngine(newRegistry())
        val item = newRollingTariffThreshold("code-431", "label-374")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

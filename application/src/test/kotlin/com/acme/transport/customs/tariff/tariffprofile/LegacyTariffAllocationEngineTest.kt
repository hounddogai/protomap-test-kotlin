package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTariffAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTariffAllocationEngine(newRegistry())
        val item = newLegacyTariffAllocation("code-366", "name-803")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

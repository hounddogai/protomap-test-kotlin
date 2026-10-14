package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyEvChargeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyEvChargeMappingEngine(newRegistry())
        val item = newLegacyEvChargeMapping("code-777", "name-851")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

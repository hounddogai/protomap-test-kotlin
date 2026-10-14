package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyZoneMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyZoneMappingEngine(newRegistry())
        val item = newLegacyZoneMapping("code-759", "label-256")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

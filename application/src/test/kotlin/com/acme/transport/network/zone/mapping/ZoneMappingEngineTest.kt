package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneMappingEngine(newRegistry())
        val item = newZoneMapping("code-413", "name-609")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

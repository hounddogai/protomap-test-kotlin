package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneTemplateEngine(newRegistry())
        val item = newZoneTemplate("code-577", "name-159")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

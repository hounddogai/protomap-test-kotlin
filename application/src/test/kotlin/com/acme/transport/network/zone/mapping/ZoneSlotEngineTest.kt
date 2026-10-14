package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneSlotEngine(newRegistry())
        val item = newZoneSlot("code-391", "title-981")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

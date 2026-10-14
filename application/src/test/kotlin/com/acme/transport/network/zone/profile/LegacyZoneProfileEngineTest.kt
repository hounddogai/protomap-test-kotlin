package com.acme.transport.network.zone.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyZoneProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyZoneProfileEngine(newRegistry())
        val item = newLegacyZoneProfile("code-481", "label-344")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

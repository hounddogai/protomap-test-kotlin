package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyZoneOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyZoneOverrideEngine()
        val item = newLegacyZoneOverride("code-180", "title-564")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

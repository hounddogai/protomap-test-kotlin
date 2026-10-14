package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredZoneManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredZoneManifestEngine()
        val item = newTieredZoneManifest("code-931", "label-109")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

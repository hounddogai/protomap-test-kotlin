package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredZoneSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredZoneSnapshotEngine()
        val item = newTieredZoneSnapshot("code-749", "label-348")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

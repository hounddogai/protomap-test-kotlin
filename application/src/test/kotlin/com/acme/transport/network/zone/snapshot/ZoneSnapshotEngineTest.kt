package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneSnapshotEngine(newRegistry())
        val item = newZoneSnapshot("code-52", "title-268")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

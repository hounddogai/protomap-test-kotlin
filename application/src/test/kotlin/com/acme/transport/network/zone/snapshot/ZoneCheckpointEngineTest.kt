package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneCheckpointEngine(newRegistry())
        val item = newZoneCheckpoint("code-872", "label-282")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

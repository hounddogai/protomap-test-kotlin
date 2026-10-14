package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledZoneCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledZoneCheckpointEngine(newRegistry())
        val item = newScheduledZoneCheckpoint("code-774", "name-381")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

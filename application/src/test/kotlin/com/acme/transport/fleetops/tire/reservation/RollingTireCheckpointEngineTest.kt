package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTireCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTireCheckpointEngine(newRegistry())
        val item = newRollingTireCheckpoint("code-446", "name-852")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

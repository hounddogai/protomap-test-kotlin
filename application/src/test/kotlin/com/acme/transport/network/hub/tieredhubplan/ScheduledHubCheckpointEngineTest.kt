package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHubCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHubCheckpointEngine(newRegistry())
        val item = newScheduledHubCheckpoint("code-587", "name-864")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

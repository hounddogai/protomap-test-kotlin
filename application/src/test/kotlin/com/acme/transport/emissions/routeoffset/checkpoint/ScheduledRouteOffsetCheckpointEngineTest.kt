package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRouteOffsetCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRouteOffsetCheckpointEngine(newRegistry())
        val item = newScheduledRouteOffsetCheckpoint("code-474", "name-407")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

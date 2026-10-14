package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetCheckpointEngine(newRegistry())
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRouteOffsetCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRouteOffsetCheckpointEngine()
        val item = newStrictRouteOffsetCheckpoint("code-743", "title-302")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

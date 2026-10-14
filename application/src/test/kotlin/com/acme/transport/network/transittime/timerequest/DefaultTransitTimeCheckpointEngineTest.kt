package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTransitTimeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTransitTimeCheckpointEngine()
        val item = newDefaultTransitTimeCheckpoint("code-153", "name-512")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

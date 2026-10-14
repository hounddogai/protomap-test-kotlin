package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaCheckpointEngine()
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

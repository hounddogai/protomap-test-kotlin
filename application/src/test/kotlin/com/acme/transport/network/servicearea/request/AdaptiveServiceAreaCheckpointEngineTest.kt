package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveServiceAreaCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveServiceAreaCheckpointEngine(newRegistry())
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

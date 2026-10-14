package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackIdlingCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackIdlingCheckpointEngine()
        val item = newFallbackIdlingCheckpoint("code-809", "name-303")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

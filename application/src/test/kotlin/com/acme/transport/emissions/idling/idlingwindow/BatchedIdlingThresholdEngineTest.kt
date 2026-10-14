package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedIdlingThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedIdlingThresholdEngine(newRegistry())
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

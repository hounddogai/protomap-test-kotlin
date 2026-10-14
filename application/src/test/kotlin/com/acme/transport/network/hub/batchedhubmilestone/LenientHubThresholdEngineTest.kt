package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientHubThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientHubThresholdEngine()
        val item = newLenientHubThreshold("code-981", "name-185")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

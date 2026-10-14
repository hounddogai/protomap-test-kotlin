package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingThresholdEngine()
        val item = newIdlingThreshold("code-907", "title-709")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

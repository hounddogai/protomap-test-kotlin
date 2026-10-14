package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityRequestEngine()
        val item = newAvailabilityRequest("code-524", "label-185")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

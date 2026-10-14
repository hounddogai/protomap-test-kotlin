package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityRecordEngine()
        val item = newAvailabilityRecord("code-936", "title-949")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

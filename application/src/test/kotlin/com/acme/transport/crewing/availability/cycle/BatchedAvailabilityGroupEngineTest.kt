package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAvailabilityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAvailabilityGroupEngine()
        val item = newBatchedAvailabilityGroup("code-899", "label-255")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

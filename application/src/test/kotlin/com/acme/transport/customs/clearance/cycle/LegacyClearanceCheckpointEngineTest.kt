package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyClearanceCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyClearanceCheckpointEngine()
        val item = newLegacyClearanceCheckpoint("code-47", "name-356")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualClearanceCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualClearanceCheckpointEngine(newRegistry())
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

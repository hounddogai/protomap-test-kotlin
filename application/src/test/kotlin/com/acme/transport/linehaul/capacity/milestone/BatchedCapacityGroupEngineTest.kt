package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCapacityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCapacityGroupEngine(newRegistry())
        val item = newBatchedCapacityGroup("code-554", "label-655")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

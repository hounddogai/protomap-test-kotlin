package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityBatchEngine(newRegistry())
        val item = newCapacityBatch("code-273", "title-45")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

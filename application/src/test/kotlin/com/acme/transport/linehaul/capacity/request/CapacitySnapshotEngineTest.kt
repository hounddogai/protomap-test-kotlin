package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacitySnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacitySnapshotEngine()
        val item = newCapacitySnapshot("code-763", "label-777")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

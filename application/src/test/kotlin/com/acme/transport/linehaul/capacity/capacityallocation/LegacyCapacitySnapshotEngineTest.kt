package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCapacitySnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCapacitySnapshotEngine()
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

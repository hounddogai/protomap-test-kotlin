package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCapacityManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCapacityManifestEngine()
        val item = newBatchedCapacityManifest("code-980", "name-192")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

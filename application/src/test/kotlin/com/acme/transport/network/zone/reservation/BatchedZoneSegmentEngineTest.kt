package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedZoneSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedZoneSegmentEngine(newRegistry())
        val item = newBatchedZoneSegment("code-377", "name-511")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

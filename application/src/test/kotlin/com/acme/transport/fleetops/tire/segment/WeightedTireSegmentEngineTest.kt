package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTireSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTireSegmentEngine(newRegistry())
        val item = newWeightedTireSegment("code-266", "name-615")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

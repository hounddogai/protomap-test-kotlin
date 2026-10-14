package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCapacityRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCapacityRequestEngine()
        val item = newLenientCapacityRequest("code-87", "title-401")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

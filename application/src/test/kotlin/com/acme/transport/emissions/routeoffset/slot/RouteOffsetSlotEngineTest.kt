package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetSlotEngine(newRegistry())
        val item = newRouteOffsetSlot("code-358", "title-168")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetScheduleEngine(newRegistry())
        val item = newRouteOffsetSchedule("code-878", "title-836")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

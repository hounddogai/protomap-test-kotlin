package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRouteOffsetScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRouteOffsetScheduleEngine()
        val item = newFallbackRouteOffsetSchedule("code-190", "name-952")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

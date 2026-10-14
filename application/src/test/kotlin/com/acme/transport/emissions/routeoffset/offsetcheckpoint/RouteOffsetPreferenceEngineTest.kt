package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetPreferenceEngine(newRegistry())
        val item = newRouteOffsetPreference("code-579", "label-453")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

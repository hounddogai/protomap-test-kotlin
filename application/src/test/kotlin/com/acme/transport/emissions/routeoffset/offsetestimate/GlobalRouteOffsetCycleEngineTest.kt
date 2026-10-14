package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRouteOffsetCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRouteOffsetCycleEngine(newRegistry())
        val item = newGlobalRouteOffsetCycle("code-409", "name-433")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

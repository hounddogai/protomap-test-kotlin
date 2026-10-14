package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRouteOffsetRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRouteOffsetRequestEngine(newRegistry())
        val item = newCappedRouteOffsetRequest("code-907", "name-97")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

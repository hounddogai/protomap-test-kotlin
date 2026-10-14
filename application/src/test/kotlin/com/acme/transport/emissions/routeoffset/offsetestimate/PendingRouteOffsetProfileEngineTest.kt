package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRouteOffsetProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRouteOffsetProfileEngine(newRegistry())
        val item = newPendingRouteOffsetProfile("code-864", "title-763")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetQuotaEngine(newRegistry())
        val item = newRouteOffsetQuota("code-801", "label-655")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

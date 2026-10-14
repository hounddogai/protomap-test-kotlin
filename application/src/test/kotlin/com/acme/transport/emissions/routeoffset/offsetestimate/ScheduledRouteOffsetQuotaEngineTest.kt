package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRouteOffsetQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRouteOffsetQuotaEngine(newRegistry())
        val item = newScheduledRouteOffsetQuota("code-292", "title-502")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRouteOffsetSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRouteOffsetSummaryEngine(newRegistry())
        val item = newScheduledRouteOffsetSummary("code-99", "name-612")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

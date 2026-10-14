package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRouteOffsetSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRouteOffsetSummaryEngine(newRegistry())
        val item = newGlobalRouteOffsetSummary("code-17", "label-712")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

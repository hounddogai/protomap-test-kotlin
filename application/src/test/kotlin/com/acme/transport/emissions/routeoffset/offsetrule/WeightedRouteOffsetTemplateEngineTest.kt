package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRouteOffsetTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRouteOffsetTemplateEngine(newRegistry())
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

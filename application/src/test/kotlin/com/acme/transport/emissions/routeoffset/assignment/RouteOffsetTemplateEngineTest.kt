package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetTemplateEngine(newRegistry())
        val item = newRouteOffsetTemplate("code-620", "name-672")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

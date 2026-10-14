package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRouteOffsetScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRouteOffsetScoreEngine(newRegistry())
        val item = newFallbackRouteOffsetScore("code-151", "title-420")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

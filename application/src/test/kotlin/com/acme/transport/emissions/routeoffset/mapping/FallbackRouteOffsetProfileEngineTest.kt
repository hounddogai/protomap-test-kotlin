package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRouteOffsetProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRouteOffsetProfileEngine(newRegistry())
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

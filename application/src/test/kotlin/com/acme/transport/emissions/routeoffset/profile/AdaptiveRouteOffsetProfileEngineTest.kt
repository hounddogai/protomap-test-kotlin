package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveRouteOffsetProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveRouteOffsetProfileEngine(newRegistry())
        val item = newAdaptiveRouteOffsetProfile("code-675", "title-679")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

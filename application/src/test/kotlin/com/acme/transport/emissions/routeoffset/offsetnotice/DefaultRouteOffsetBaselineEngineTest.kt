package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRouteOffsetBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRouteOffsetBaselineEngine(newRegistry())
        val item = newDefaultRouteOffsetBaseline("code-294", "label-868")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

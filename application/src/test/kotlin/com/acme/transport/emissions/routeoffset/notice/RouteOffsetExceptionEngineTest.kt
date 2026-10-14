package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetExceptionEngine(newRegistry())
        val item = newRouteOffsetException("code-614", "title-402")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRouteOffsetDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRouteOffsetDigestEngine()
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

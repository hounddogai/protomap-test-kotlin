package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRouteOffsetDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRouteOffsetDigestEngine(newRegistry())
        val item = newPendingRouteOffsetDigest("code-173", "title-52")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRouteOffsetDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRouteOffsetDigestEngine(newRegistry())
        val item = newBatchedRouteOffsetDigest("code-811", "name-822")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

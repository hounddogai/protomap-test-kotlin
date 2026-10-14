package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRouteOffsetQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRouteOffsetQuotaEngine()
        val item = newGlobalRouteOffsetQuota("code-80", "label-776")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

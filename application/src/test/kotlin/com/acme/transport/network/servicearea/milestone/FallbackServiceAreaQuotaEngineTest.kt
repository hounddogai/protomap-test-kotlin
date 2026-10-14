package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServiceAreaQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServiceAreaQuotaEngine()
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

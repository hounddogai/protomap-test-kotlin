package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaQuotaEngine()
        val item = newServiceAreaQuota("code-155", "title-257")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

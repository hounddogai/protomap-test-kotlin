package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLanePlanQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLanePlanQuotaEngine()
        val item = newLegacyLanePlanQuota("code-329", "title-877")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

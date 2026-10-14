package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServiceAreaQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServiceAreaQuotaEngine()
        val item = newSeasonalServiceAreaQuota("code-931", "label-265")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

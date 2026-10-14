package com.acme.transport.fleetops.tire.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTireQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTireQuotaEngine()
        val item = newSeasonalTireQuota("code-608", "name-247")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

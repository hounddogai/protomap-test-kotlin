package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCarbonReportQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCarbonReportQuotaEngine(newRegistry())
        val item = newTieredCarbonReportQuota("code-416", "label-671")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

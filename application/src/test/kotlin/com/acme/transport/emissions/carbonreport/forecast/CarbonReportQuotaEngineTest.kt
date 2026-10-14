package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportQuotaEngine(newRegistry())
        val item = newCarbonReportQuota("code-438", "title-768")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

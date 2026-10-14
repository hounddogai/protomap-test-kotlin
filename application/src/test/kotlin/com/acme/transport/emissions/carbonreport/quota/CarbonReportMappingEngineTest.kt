package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportMappingEngine(newRegistry())
        val item = newCarbonReportMapping("code-267", "title-759")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

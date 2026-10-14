package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCarbonReportMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCarbonReportMappingEngine(newRegistry())
        val item = newStrictCarbonReportMapping("code-53", "label-684")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

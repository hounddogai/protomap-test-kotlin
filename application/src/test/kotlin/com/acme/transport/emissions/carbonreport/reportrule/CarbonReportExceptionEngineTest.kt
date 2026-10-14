package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportExceptionEngine()
        val item = newCarbonReportException("code-387", "label-252")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

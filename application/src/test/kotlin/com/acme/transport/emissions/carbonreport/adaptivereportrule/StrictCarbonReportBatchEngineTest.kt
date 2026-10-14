package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCarbonReportBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCarbonReportBatchEngine()
        val item = newStrictCarbonReportBatch("code-321", "title-73")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

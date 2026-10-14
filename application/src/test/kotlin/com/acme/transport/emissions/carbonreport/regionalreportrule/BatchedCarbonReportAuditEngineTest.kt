package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCarbonReportAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCarbonReportAuditEngine()
        val item = newBatchedCarbonReportAudit("code-838", "name-617")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

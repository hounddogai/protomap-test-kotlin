package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCarbonReportGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCarbonReportGroupEngine()
        val item = newCappedCarbonReportGroup("code-517", "name-190")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

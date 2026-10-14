package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCarbonReportAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCarbonReportAgreementEngine(newRegistry())
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

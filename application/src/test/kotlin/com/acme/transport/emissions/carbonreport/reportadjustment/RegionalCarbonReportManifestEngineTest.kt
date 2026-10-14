package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCarbonReportManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCarbonReportManifestEngine(newRegistry())
        val item = newRegionalCarbonReportManifest("code-182", "title-224")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

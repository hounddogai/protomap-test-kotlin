package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCarbonReportManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCarbonReportManifestEngine()
        val item = newLenientCarbonReportManifest("code-794", "label-764")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

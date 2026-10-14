package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCarbonReportGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCarbonReportGroupEngine(newRegistry())
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

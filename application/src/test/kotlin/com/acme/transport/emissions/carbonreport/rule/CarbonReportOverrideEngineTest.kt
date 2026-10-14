package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportOverrideEngine(newRegistry())
        val item = newCarbonReportOverride("code-444", "name-601")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

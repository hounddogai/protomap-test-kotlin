package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCarbonReportDigestEngine(newRegistry())
        val item = newCarbonReportDigest("code-796", "title-196")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

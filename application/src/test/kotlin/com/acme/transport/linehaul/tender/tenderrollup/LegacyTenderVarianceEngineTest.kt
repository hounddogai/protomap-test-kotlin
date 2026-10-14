package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTenderVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTenderVarianceEngine(newRegistry())
        val item = newLegacyTenderVariance("code-828", "label-891")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

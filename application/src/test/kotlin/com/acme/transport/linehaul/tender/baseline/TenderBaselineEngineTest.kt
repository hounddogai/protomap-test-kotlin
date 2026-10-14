package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderBaselineEngine()
        val item = newTenderBaseline("code-797", "label-857")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

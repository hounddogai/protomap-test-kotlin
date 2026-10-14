package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderVarianceEngine(newRegistry())
        val item = newTenderVariance("code-800", "title-429")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

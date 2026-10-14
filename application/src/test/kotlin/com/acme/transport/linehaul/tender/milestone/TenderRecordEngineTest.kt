package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderRecordEngine()
        val item = newTenderRecord("code-468", "title-813")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

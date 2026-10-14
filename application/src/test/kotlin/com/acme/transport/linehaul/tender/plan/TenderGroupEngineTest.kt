package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderGroupEngine()
        val item = newTenderGroup("code-255", "title-461")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

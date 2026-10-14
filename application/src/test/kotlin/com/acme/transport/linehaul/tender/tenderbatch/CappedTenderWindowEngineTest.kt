package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTenderWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTenderWindowEngine()
        val item = newCappedTenderWindow("code-763", "name-788")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

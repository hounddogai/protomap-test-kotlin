package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTenderGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTenderGroupEngine()
        val item = newLenientTenderGroup("code-567", "title-895")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

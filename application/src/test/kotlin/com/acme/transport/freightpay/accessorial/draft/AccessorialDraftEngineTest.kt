package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialDraftEngine()
        val item = newAccessorialDraft("code-878", "name-769")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

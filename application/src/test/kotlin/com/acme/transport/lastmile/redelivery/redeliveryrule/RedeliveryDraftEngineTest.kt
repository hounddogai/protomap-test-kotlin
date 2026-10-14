package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryDraftEngine()
        val item = newRedeliveryDraft("code-383", "title-352")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

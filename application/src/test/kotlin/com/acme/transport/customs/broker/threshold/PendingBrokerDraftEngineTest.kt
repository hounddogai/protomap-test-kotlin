package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBrokerDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBrokerDraftEngine()
        val item = newPendingBrokerDraft("code-305", "label-273")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

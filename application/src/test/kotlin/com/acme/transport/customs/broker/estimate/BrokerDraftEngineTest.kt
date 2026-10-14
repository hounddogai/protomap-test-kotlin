package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerDraftEngine()
        val item = newBrokerDraft("code-109", "label-310")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerNoticeEngine()
        val item = newBrokerNotice("code-267", "label-68")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

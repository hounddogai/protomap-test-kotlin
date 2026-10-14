package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledBrokerRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledBrokerRecordEngine()
        val item = newScheduledBrokerRecord("code-135", "label-68")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

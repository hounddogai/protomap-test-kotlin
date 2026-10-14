package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBrokerQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBrokerQuotaEngine()
        val item = newManualBrokerQuota("code-864", "name-293")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

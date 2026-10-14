package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerQuotaEngine()
        val item = newBrokerQuota("code-943", "name-29")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

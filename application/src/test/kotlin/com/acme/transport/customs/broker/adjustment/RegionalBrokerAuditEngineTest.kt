package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalBrokerAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalBrokerAuditEngine()
        val item = newRegionalBrokerAudit("code-213", "title-148")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

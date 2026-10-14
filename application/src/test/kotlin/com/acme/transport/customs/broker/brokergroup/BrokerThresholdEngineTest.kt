package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerThresholdEngine(newRegistry())
        val item = newBrokerThreshold("code-84", "title-238")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

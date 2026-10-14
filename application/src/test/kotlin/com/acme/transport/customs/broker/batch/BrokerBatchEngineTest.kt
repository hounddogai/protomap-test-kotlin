package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerBatchEngine(newRegistry())
        val item = newBrokerBatch("code-6", "label-893")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

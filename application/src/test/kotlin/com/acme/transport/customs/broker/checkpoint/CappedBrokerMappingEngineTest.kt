package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBrokerMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBrokerMappingEngine(newRegistry())
        val item = newCappedBrokerMapping("code-305", "label-643")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

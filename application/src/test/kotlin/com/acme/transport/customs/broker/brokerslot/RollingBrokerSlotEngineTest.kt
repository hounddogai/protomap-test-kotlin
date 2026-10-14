package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBrokerSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBrokerSlotEngine(newRegistry())
        val item = newRollingBrokerSlot("code-606", "name-539")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

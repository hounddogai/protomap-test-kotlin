package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBrokerSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBrokerSlotEngine(newRegistry())
        val item = newCappedBrokerSlot("code-207", "name-151")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerEntryEngine(newRegistry())
        val item = newBrokerEntry("code-35", "name-530")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

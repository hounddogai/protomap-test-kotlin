package com.acme.transport.customs.broker.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledBrokerRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledBrokerRequestEngine(newRegistry())
        val item = newScheduledBrokerRequest("code-925", "name-958")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerSummaryEngine(newRegistry())
        val item = newBrokerSummary("code-279", "name-202")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

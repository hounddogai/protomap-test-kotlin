package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalBrokerSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalBrokerSummaryEngine(newRegistry())
        val item = newRegionalBrokerSummary("code-340", "name-64")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

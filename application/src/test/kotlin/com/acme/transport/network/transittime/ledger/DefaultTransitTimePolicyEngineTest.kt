package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTransitTimePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTransitTimePolicyEngine(newRegistry())
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

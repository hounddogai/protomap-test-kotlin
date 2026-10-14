package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayPlanEngine()
        val item = newRelayPlan("code-438", "title-93")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

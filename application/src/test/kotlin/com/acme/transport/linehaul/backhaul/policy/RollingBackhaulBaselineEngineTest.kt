package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBackhaulBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBackhaulBaselineEngine()
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

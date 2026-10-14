package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldEstimateEngine()
        val item = newHoldEstimate("code-547", "name-507")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

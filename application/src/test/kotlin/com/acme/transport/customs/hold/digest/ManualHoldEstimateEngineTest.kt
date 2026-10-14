package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualHoldEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualHoldEstimateEngine()
        val item = newManualHoldEstimate("code-96", "title-495")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

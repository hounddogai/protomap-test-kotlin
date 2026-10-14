package com.acme.transport.crewing.availability.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityRuleEngine()
        val item = newAvailabilityRule("code-703", "title-857")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

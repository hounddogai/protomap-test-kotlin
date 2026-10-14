package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveViolationScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveViolationScoreEngine(newRegistry())
        val item = newAdaptiveViolationScore("code-47", "label-863")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

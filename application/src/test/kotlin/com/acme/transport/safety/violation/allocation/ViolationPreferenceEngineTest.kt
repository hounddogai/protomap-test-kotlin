package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationPreferenceEngine()
        val item = newViolationPreference("code-166", "title-661")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

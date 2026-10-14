package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingViolationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingViolationProfileEngine()
        val item = newRollingViolationProfile("code-722", "title-720")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationNoticeEngine()
        val item = newViolationNotice("code-515", "label-873")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

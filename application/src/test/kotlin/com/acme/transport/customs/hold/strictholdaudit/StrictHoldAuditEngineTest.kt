package com.acme.transport.customs.hold.strictholdaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictHoldAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictHoldAuditEngine()
        val item = newStrictHoldAudit("code-249", "label-640")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

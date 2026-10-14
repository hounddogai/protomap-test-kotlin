package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingClearanceOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingClearanceOverrideEngine()
        val item = newPendingClearanceOverride("code-471", "label-345")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

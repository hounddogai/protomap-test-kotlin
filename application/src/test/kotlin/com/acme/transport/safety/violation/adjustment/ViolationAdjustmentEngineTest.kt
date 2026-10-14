package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationAdjustmentEngine(newRegistry())
        val item = newViolationAdjustment("code-528", "name-442")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

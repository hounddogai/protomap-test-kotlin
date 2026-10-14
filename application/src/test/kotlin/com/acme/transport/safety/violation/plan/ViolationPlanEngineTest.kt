package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationPlanEngine(newRegistry())
        val item = newViolationPlan("code-635", "label-668")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

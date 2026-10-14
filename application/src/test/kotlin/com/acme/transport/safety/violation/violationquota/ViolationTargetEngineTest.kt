package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationTargetEngine(newRegistry())
        val item = newViolationTarget("code-77", "label-181")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

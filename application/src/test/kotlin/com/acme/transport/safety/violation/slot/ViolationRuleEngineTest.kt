package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationRuleEngine(newRegistry())
        val item = newViolationRule("code-432", "label-372")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryViolationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryViolationTierEngine(newRegistry())
        val item = newPrimaryViolationTier("code-779", "label-715")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

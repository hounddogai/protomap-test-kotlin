package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalViolationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalViolationTierEngine(newRegistry())
        val item = newGlobalViolationTier("code-45", "name-105")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

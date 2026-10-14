package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackViolationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackViolationSlotEngine(newRegistry())
        val item = newFallbackViolationSlot("code-228", "title-82")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

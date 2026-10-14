package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationSummaryEngine(newRegistry())
        val item = newViolationSummary("code-790", "name-380")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationEntryEngine(newRegistry())
        val item = newViolationEntry("code-982", "name-88")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

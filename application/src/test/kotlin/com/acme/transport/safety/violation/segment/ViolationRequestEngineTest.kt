package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationRequestEngine(newRegistry())
        val item = newViolationRequest("code-509", "name-302")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

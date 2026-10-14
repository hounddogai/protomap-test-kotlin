package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationGroupEngine()
        val item = newViolationGroup("code-280", "title-692")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

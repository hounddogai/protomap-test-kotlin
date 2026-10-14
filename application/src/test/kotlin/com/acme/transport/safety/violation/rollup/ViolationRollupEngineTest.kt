package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationRollupEngine()
        val item = newViolationRollup("code-877", "label-40")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

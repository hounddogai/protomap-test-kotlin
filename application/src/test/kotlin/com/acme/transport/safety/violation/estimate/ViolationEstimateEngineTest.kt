package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationEstimateEngine(newRegistry())
        val item = newViolationEstimate("code-213", "name-879")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

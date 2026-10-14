package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationClaimEngine(newRegistry())
        val item = newViolationClaim("code-166", "title-500")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

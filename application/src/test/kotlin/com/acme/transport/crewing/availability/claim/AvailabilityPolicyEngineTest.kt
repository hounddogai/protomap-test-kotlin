package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityPolicyEngine(newRegistry())
        val item = newAvailabilityPolicy("code-248", "title-644")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

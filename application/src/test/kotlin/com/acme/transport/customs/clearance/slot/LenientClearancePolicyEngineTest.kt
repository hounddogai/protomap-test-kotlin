package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientClearancePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientClearancePolicyEngine(newRegistry())
        val item = newLenientClearancePolicy("code-601", "label-119")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

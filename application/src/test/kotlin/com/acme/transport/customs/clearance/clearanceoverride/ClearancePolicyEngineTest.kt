package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearancePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearancePolicyEngine(newRegistry())
        val item = newClearancePolicy("code-635", "name-638")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

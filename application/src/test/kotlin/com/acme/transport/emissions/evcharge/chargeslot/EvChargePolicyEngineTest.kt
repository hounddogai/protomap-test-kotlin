package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargePolicyEngine(newRegistry())
        val item = newEvChargePolicy("code-632", "name-537")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

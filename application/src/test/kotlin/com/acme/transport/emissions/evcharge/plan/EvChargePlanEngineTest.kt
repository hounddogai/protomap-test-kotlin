package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargePlanEngine(newRegistry())
        val item = newEvChargePlan("code-515", "title-634")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

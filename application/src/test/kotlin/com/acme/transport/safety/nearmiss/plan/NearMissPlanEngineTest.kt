package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissPlanEngine(newRegistry())
        val item = newNearMissPlan("code-764", "label-89")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

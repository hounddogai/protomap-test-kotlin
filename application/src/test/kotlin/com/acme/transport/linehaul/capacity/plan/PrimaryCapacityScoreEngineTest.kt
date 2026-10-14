package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCapacityScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCapacityScoreEngine(newRegistry())
        val item = newPrimaryCapacityScore("code-940", "name-529")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

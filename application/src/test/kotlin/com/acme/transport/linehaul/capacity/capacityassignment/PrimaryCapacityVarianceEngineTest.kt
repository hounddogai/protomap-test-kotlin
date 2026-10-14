package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCapacityVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCapacityVarianceEngine(newRegistry())
        val item = newPrimaryCapacityVariance("code-485", "title-80")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityEstimateEngine(newRegistry())
        val item = newCapacityEstimate("code-42", "title-456")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

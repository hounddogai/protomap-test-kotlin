package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnSegmentEngine(newRegistry())
        val item = newFuelBurnSegment("code-472", "label-864")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelBurnTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelBurnTargetEngine(newRegistry())
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

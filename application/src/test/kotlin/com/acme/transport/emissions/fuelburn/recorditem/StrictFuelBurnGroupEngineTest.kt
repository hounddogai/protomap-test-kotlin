package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictFuelBurnGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictFuelBurnGroupEngine(newRegistry())
        val item = newStrictFuelBurnGroup("code-174", "label-353")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

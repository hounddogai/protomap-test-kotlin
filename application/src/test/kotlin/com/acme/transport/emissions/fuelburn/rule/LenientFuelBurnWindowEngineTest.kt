package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFuelBurnWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFuelBurnWindowEngine(newRegistry())
        val item = newLenientFuelBurnWindow("code-620", "title-111")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

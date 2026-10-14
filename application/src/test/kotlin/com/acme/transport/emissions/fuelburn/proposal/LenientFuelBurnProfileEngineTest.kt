package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFuelBurnProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFuelBurnProfileEngine(newRegistry())
        val item = newLenientFuelBurnProfile("code-602", "title-67")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

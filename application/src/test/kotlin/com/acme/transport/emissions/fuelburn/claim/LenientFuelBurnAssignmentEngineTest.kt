package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFuelBurnAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFuelBurnAssignmentEngine(newRegistry())
        val item = newLenientFuelBurnAssignment("code-441", "title-505")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

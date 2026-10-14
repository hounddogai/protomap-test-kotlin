package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultFuelBurnAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultFuelBurnAssignmentEngine(newRegistry())
        val item = newDefaultFuelBurnAssignment("code-222", "title-900")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

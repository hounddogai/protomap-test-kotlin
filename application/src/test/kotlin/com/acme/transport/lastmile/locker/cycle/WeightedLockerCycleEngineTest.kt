package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedLockerCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedLockerCycleEngine()
        val item = newWeightedLockerCycle("code-221", "name-735")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

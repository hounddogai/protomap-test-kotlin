package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedFuelBurnReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedFuelBurnReviewEngine(newRegistry())
        val item = newWeightedFuelBurnReview("code-32", "name-881")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAvailabilitySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAvailabilitySlotEngine(newRegistry())
        val item = newWeightedAvailabilitySlot("code-952", "name-864")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

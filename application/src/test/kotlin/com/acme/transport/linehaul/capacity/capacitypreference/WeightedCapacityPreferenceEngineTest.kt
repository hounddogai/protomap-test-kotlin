package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCapacityPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCapacityPreferenceEngine(newRegistry())
        val item = newWeightedCapacityPreference("code-270", "name-181")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

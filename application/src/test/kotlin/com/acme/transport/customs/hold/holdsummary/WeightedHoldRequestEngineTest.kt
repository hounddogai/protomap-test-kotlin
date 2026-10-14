package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedHoldRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedHoldRequestEngine(newRegistry())
        val item = newWeightedHoldRequest("code-753", "name-990")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

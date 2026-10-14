package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBackhaulTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBackhaulTargetEngine(newRegistry())
        val item = newWeightedBackhaulTarget("code-726", "name-161")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

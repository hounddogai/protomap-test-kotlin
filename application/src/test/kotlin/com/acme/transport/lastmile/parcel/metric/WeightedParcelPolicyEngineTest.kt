package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelPolicyEngine(newRegistry())
        val item = newWeightedParcelPolicy("code-980", "label-963")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

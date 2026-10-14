package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelTemplateEngine(newRegistry())
        val item = newWeightedParcelTemplate("code-811", "label-394")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

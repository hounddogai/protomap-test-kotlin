package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelOverrideEngine(newRegistry())
        val item = newWeightedParcelOverride("code-607", "name-973")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

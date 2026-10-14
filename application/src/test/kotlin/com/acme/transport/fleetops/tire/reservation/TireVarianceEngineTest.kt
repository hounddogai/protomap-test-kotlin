package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireVarianceEngine(newRegistry())
        val item = newTireVariance("code-81", "label-722")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

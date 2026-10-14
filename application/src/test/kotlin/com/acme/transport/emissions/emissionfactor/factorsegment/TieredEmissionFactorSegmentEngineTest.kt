package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEmissionFactorSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEmissionFactorSegmentEngine(newRegistry())
        val item = newTieredEmissionFactorSegment("code-363", "name-63")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

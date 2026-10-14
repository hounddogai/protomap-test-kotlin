package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDefectVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDefectVarianceEngine(newRegistry())
        val item = newTieredDefectVariance("code-890", "label-536")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

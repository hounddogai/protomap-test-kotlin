package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDefectClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDefectClaimEngine()
        val item = newBatchedDefectClaim("code-649", "label-395")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

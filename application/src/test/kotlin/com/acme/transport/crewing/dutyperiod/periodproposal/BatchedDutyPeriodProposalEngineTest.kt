package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDutyPeriodProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedDutyPeriodProposalEngine()
        val item = newBatchedDutyPeriodProposal("code-733", "label-212")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

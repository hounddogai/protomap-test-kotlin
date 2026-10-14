package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDutyPeriodProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDutyPeriodProposalEngine()
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}

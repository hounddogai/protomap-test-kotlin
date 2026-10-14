package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDutyPeriodProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        assertEquals(LenientDutyPeriodProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        try {
            item.moveTo(LenientDutyPeriodProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDutyPeriodProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        item.status = LenientDutyPeriodProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientDutyPeriodProposalStatus.PLANNED) }
    }
}

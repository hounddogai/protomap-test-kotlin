package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodClaim("code-379", "title-970")
        assertEquals(DutyPeriodClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDutyPeriodClaim("code-379", "title-970")
        try {
            item.moveTo(DutyPeriodClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodClaim("code-379", "title-970")
        item.status = DutyPeriodClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodClaimStatus.PROPOSED) }
    }
}

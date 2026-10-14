package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanClaimTest {
    @Test
    fun `starts in open`() {
        val item = newLanePlanClaim("code-355", "title-392")
        assertEquals(LanePlanClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLanePlanClaim("code-355", "title-392")
        try {
            item.moveTo(LanePlanClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLanePlanClaim("code-355", "title-392")
        item.status = LanePlanClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(LanePlanClaimStatus.OPEN) }
    }
}

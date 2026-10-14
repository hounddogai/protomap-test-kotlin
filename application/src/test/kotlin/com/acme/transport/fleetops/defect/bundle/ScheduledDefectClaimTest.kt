package com.acme.transport.fleetops.defect.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledDefectClaim("code-10", "title-418")
        assertEquals(ScheduledDefectClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledDefectClaim("code-10", "title-418")
        try {
            item.moveTo(ScheduledDefectClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledDefectClaim("code-10", "title-418")
        item.status = ScheduledDefectClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledDefectClaimStatus.PROPOSED) }
    }
}

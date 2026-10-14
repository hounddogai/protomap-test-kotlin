package com.acme.transport.lastmile.parcel.tieredparcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualParcelClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualParcelClaim("code-31", "name-750")
        assertEquals(ManualParcelClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualParcelClaim("code-31", "name-750")
        try {
            item.moveTo(ManualParcelClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualParcelClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualParcelClaim("code-31", "name-750")
        item.status = ManualParcelClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualParcelClaimStatus.PROPOSED) }
    }
}

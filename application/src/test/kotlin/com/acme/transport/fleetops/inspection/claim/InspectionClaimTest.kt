package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionClaim("code-295", "title-807")
        assertEquals(InspectionClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newInspectionClaim("code-295", "title-807")
        try {
            item.moveTo(InspectionClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionClaim("code-295", "title-807")
        item.status = InspectionClaimStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionClaimStatus.DRAFT) }
    }
}

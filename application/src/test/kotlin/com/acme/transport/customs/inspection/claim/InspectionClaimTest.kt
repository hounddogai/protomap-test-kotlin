package com.acme.transport.customs.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionClaimTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionClaim("code-861", "title-268")
        assertEquals(InspectionClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newInspectionClaim("code-861", "title-268")
        try {
            item.moveTo(InspectionClaimStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionClaimStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionClaim("code-861", "title-268")
        item.status = InspectionClaimStatus.FAILED
        assertFails("move") { item.moveTo(InspectionClaimStatus.NEW) }
    }
}

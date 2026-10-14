package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDefectClaimTest {
    @Test
    fun `starts in open`() {
        val item = newManualDefectClaim("code-751", "title-861")
        assertEquals(ManualDefectClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newManualDefectClaim("code-751", "title-861")
        try {
            item.moveTo(ManualDefectClaimStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDefectClaimStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualDefectClaim("code-751", "title-861")
        item.status = ManualDefectClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualDefectClaimStatus.OPEN) }
    }
}

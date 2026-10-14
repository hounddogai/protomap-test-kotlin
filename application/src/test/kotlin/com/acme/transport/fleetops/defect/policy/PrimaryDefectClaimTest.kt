package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDefectClaimTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryDefectClaim("code-401", "title-979")
        assertEquals(PrimaryDefectClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryDefectClaim("code-401", "title-979")
        try {
            item.moveTo(PrimaryDefectClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDefectClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryDefectClaim("code-401", "title-979")
        item.status = PrimaryDefectClaimStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryDefectClaimStatus.CREATED) }
    }
}

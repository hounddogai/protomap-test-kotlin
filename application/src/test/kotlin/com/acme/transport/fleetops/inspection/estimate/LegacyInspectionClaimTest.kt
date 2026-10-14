package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyInspectionClaim("code-949", "title-797")
        assertEquals(LegacyInspectionClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyInspectionClaim("code-949", "title-797")
        try {
            item.moveTo(LegacyInspectionClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyInspectionClaim("code-949", "title-797")
        item.status = LegacyInspectionClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyInspectionClaimStatus.PLANNED) }
    }
}

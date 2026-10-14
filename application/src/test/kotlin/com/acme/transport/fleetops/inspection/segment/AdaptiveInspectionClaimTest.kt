package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveInspectionClaim("code-529", "title-715")
        assertEquals(AdaptiveInspectionClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveInspectionClaim("code-529", "title-715")
        try {
            item.moveTo(AdaptiveInspectionClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveInspectionClaim("code-529", "title-715")
        item.status = AdaptiveInspectionClaimStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveInspectionClaimStatus.IDLE) }
    }
}

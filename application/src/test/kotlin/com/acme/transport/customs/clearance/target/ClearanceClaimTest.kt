package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceClaimTest {
    @Test
    fun `starts in created`() {
        val item = newClearanceClaim("code-867", "label-978")
        assertEquals(ClearanceClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newClearanceClaim("code-867", "label-978")
        try {
            item.moveTo(ClearanceClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newClearanceClaim("code-867", "label-978")
        item.status = ClearanceClaimStatus.RETIRED
        assertFails("move") { item.moveTo(ClearanceClaimStatus.CREATED) }
    }
}

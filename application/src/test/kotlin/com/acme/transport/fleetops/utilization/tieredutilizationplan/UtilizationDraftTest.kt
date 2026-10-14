package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newUtilizationDraft("code-818", "label-135")
        assertEquals(UtilizationDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newUtilizationDraft("code-818", "label-135")
        try {
            item.moveTo(UtilizationDraftStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationDraftStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newUtilizationDraft("code-818", "label-135")
        item.status = UtilizationDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(UtilizationDraftStatus.REQUESTED) }
    }
}

package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnDraftTest {
    @Test
    fun `starts in created`() {
        val item = newLeaseReturnDraft("code-964", "name-333")
        assertEquals(LeaseReturnDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLeaseReturnDraft("code-964", "name-333")
        try {
            item.moveTo(LeaseReturnDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLeaseReturnDraft("code-964", "name-333")
        item.status = LeaseReturnDraftStatus.RETIRED
        assertFails("move") { item.moveTo(LeaseReturnDraftStatus.CREATED) }
    }
}

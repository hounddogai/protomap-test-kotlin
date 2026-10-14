package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLeaseReturnDraftTest {
    @Test
    fun `starts in open`() {
        val item = newStrictLeaseReturnDraft("code-885", "label-813")
        assertEquals(StrictLeaseReturnDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictLeaseReturnDraft("code-885", "label-813")
        try {
            item.moveTo(StrictLeaseReturnDraftStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLeaseReturnDraftStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictLeaseReturnDraft("code-885", "label-813")
        item.status = StrictLeaseReturnDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictLeaseReturnDraftStatus.OPEN) }
    }
}

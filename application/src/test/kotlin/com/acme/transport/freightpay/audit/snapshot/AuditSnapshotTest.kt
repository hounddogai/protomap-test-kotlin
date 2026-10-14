package com.acme.transport.freightpay.audit.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditSnapshot("code-656", "name-144")
        assertEquals(AuditSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAuditSnapshot("code-656", "name-144")
        try {
            item.moveTo(AuditSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditSnapshot("code-656", "name-144")
        item.status = AuditSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditSnapshotStatus.PROPOSED) }
    }
}

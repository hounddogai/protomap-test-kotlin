package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newAttemptSnapshot("code-219", "title-131")
        assertEquals(AttemptSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAttemptSnapshot("code-219", "title-131")
        try {
            item.moveTo(AttemptSnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptSnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAttemptSnapshot("code-219", "title-131")
        item.status = AttemptSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(AttemptSnapshotStatus.REQUESTED) }
    }
}

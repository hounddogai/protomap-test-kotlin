package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAttemptDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryAttemptDigest("code-661", "label-173")
        assertEquals(PrimaryAttemptDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryAttemptDigest("code-661", "label-173")
        try {
            item.moveTo(PrimaryAttemptDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAttemptDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryAttemptDigest("code-661", "label-173")
        item.status = PrimaryAttemptDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryAttemptDigestStatus.REQUESTED) }
    }
}

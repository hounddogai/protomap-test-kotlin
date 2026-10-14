package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAttemptBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryAttemptBundle("code-202", "title-258")
        assertEquals(PrimaryAttemptBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newPrimaryAttemptBundle("code-202", "title-258")
        try {
            item.moveTo(PrimaryAttemptBundleStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAttemptBundleStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryAttemptBundle("code-202", "title-258")
        item.status = PrimaryAttemptBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryAttemptBundleStatus.PLANNED) }
    }
}

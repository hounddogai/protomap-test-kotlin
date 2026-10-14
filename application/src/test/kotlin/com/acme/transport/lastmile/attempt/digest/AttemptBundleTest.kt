package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newAttemptBundle("code-72", "name-532")
        assertEquals(AttemptBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAttemptBundle("code-72", "name-532")
        try {
            item.moveTo(AttemptBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAttemptBundle("code-72", "name-532")
        item.status = AttemptBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AttemptBundleStatus.PROPOSED) }
    }
}

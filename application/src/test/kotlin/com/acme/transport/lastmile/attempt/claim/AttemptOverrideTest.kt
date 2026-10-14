package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptOverride("code-999", "title-259")
        assertEquals(AttemptOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAttemptOverride("code-999", "title-259")
        try {
            item.moveTo(AttemptOverrideStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptOverrideStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptOverride("code-999", "title-259")
        item.status = AttemptOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptOverrideStatus.OPEN) }
    }
}

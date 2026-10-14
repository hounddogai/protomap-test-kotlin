package com.acme.transport.safety.violation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingViolationDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingViolationDigest("code-674", "title-443")
        assertEquals(PendingViolationDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingViolationDigest("code-674", "title-443")
        try {
            item.moveTo(PendingViolationDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingViolationDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingViolationDigest("code-674", "title-443")
        item.status = PendingViolationDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingViolationDigestStatus.PROPOSED) }
    }
}

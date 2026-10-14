package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCoachingDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedCoachingDigest("code-444", "name-189")
        assertEquals(CappedCoachingDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedCoachingDigest("code-444", "name-189")
        try {
            item.moveTo(CappedCoachingDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCoachingDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedCoachingDigest("code-444", "name-189")
        item.status = CappedCoachingDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedCoachingDigestStatus.REQUESTED) }
    }
}

package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newCoachingDigest("code-820", "title-170")
        assertEquals(CoachingDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCoachingDigest("code-820", "title-170")
        try {
            item.moveTo(CoachingDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCoachingDigest("code-820", "title-170")
        item.status = CoachingDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CoachingDigestStatus.PROPOSED) }
    }
}

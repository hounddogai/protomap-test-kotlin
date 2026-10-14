package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCoachingBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultCoachingBaseline("code-327", "name-931")
        assertEquals(DefaultCoachingBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultCoachingBaseline("code-327", "name-931")
        try {
            item.moveTo(DefaultCoachingBaselineStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCoachingBaselineStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultCoachingBaseline("code-327", "name-931")
        item.status = DefaultCoachingBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultCoachingBaselineStatus.REQUESTED) }
    }
}

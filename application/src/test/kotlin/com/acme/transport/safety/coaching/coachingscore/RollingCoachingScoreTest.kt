package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCoachingScoreTest {
    @Test
    fun `starts in new`() {
        val item = newRollingCoachingScore("code-908", "name-276")
        assertEquals(RollingCoachingScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRollingCoachingScore("code-908", "name-276")
        try {
            item.moveTo(RollingCoachingScoreStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCoachingScoreStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingCoachingScore("code-908", "name-276")
        item.status = RollingCoachingScoreStatus.FAILED
        assertFails("move") { item.moveTo(RollingCoachingScoreStatus.NEW) }
    }
}

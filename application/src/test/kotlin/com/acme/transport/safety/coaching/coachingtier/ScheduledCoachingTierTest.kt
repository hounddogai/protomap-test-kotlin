package com.acme.transport.safety.coaching.coachingtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCoachingTierTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledCoachingTier("code-396", "title-921")
        assertEquals(ScheduledCoachingTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledCoachingTier("code-396", "title-921")
        try {
            item.moveTo(ScheduledCoachingTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCoachingTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledCoachingTier("code-396", "title-921")
        item.status = ScheduledCoachingTierStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledCoachingTierStatus.NEW) }
    }
}

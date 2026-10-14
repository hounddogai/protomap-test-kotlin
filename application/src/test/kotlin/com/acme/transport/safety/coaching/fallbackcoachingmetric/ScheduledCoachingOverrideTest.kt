package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCoachingOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledCoachingOverride("code-591", "name-114")
        assertEquals(ScheduledCoachingOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledCoachingOverride("code-591", "name-114")
        try {
            item.moveTo(ScheduledCoachingOverrideStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCoachingOverrideStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledCoachingOverride("code-591", "name-114")
        item.status = ScheduledCoachingOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledCoachingOverrideStatus.PROPOSED) }
    }
}

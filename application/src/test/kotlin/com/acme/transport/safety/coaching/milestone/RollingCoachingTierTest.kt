package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCoachingTierTest {
    @Test
    fun `starts in created`() {
        val item = newRollingCoachingTier("code-421", "name-498")
        assertEquals(RollingCoachingTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingCoachingTier("code-421", "name-498")
        try {
            item.moveTo(RollingCoachingTierStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCoachingTierStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingCoachingTier("code-421", "name-498")
        item.status = RollingCoachingTierStatus.RETIRED
        assertFails("move") { item.moveTo(RollingCoachingTierStatus.CREATED) }
    }
}

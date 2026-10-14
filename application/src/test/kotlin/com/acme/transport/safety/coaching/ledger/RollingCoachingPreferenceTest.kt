package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCoachingPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingCoachingPreference("code-646", "title-103")
        assertEquals(RollingCoachingPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingCoachingPreference("code-646", "title-103")
        try {
            item.moveTo(RollingCoachingPreferenceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCoachingPreferenceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingCoachingPreference("code-646", "title-103")
        item.status = RollingCoachingPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(RollingCoachingPreferenceStatus.IDLE) }
    }
}

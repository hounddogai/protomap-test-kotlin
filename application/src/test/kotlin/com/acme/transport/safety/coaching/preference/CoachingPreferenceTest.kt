package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingPreference("code-134", "name-701")
        assertEquals(CoachingPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCoachingPreference("code-134", "name-701")
        try {
            item.moveTo(CoachingPreferenceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingPreferenceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingPreference("code-134", "name-701")
        item.status = CoachingPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingPreferenceStatus.OPEN) }
    }
}

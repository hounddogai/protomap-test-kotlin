package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimePreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledTransitTimePreference("code-188", "title-343")
        assertEquals(ScheduledTransitTimePreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledTransitTimePreference("code-188", "title-343")
        try {
            item.moveTo(ScheduledTransitTimePreferenceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimePreferenceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledTransitTimePreference("code-188", "title-343")
        item.status = ScheduledTransitTimePreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledTransitTimePreferenceStatus.OPEN) }
    }
}

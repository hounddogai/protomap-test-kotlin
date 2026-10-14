package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCutoffPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledCutoffPreference("code-47", "title-346")
        assertEquals(ScheduledCutoffPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledCutoffPreference("code-47", "title-346")
        try {
            item.moveTo(ScheduledCutoffPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCutoffPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledCutoffPreference("code-47", "title-346")
        item.status = ScheduledCutoffPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledCutoffPreferenceStatus.REQUESTED) }
    }
}

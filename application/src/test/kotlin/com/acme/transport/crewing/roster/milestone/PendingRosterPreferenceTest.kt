package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRosterPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingRosterPreference("code-213", "title-648")
        assertEquals(PendingRosterPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingRosterPreference("code-213", "title-648")
        try {
            item.moveTo(PendingRosterPreferenceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRosterPreferenceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingRosterPreference("code-213", "title-648")
        item.status = PendingRosterPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingRosterPreferenceStatus.PROPOSED) }
    }
}

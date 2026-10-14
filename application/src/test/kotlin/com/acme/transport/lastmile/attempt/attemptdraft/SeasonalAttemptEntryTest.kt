package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAttemptEntryTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalAttemptEntry("code-761", "title-91")
        assertEquals(SeasonalAttemptEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalAttemptEntry("code-761", "title-91")
        try {
            item.moveTo(SeasonalAttemptEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAttemptEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalAttemptEntry("code-761", "title-91")
        item.status = SeasonalAttemptEntryStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalAttemptEntryStatus.CREATED) }
    }
}

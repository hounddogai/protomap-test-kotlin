package com.acme.transport.lastmile.attempt.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAttemptBundleTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalAttemptBundle("code-855", "label-905")
        assertEquals(SeasonalAttemptBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalAttemptBundle("code-855", "label-905")
        try {
            item.moveTo(SeasonalAttemptBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAttemptBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalAttemptBundle("code-855", "label-905")
        item.status = SeasonalAttemptBundleStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalAttemptBundleStatus.CREATED) }
    }
}

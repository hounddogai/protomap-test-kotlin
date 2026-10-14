package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredScorecardQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredScorecardQuota("code-635", "name-379")
        assertEquals(TieredScorecardQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTieredScorecardQuota("code-635", "name-379")
        try {
            item.moveTo(TieredScorecardQuotaStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredScorecardQuotaStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredScorecardQuota("code-635", "name-379")
        item.status = TieredScorecardQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredScorecardQuotaStatus.PLANNED) }
    }
}

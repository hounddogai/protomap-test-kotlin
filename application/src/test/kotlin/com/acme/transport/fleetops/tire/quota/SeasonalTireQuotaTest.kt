package com.acme.transport.fleetops.tire.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTireQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalTireQuota("code-608", "name-247")
        assertEquals(SeasonalTireQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newSeasonalTireQuota("code-608", "name-247")
        try {
            item.moveTo(SeasonalTireQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTireQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalTireQuota("code-608", "name-247")
        item.status = SeasonalTireQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalTireQuotaStatus.OPEN) }
    }
}

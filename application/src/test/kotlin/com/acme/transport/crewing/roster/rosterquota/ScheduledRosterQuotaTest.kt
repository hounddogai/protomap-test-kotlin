package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRosterQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledRosterQuota("code-866", "title-961")
        assertEquals(ScheduledRosterQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledRosterQuota("code-866", "title-961")
        try {
            item.moveTo(ScheduledRosterQuotaStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRosterQuotaStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledRosterQuota("code-866", "title-961")
        item.status = ScheduledRosterQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledRosterQuotaStatus.CREATED) }
    }
}

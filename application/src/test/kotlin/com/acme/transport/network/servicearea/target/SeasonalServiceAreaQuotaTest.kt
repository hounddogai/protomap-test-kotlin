package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServiceAreaQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalServiceAreaQuota("code-931", "label-265")
        assertEquals(SeasonalServiceAreaQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalServiceAreaQuota("code-931", "label-265")
        try {
            item.moveTo(SeasonalServiceAreaQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServiceAreaQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalServiceAreaQuota("code-931", "label-265")
        item.status = SeasonalServiceAreaQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalServiceAreaQuotaStatus.OPEN) }
    }
}

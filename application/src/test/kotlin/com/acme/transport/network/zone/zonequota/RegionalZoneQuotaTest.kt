package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalZoneQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalZoneQuota("code-203", "name-479")
        assertEquals(RegionalZoneQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalZoneQuota("code-203", "name-479")
        try {
            item.moveTo(RegionalZoneQuotaStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalZoneQuotaStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalZoneQuota("code-203", "name-479")
        item.status = RegionalZoneQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalZoneQuotaStatus.CREATED) }
    }
}

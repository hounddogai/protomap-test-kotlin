package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledZoneNotice("code-442", "name-884")
        assertEquals(ScheduledZoneNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScheduledZoneNotice("code-442", "name-884")
        try {
            item.moveTo(ScheduledZoneNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledZoneNotice("code-442", "name-884")
        item.status = ScheduledZoneNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledZoneNoticeStatus.PLANNED) }
    }
}

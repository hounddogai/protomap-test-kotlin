package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalClearanceNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        assertEquals(SeasonalClearanceNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        try {
            item.moveTo(SeasonalClearanceNoticeStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalClearanceNoticeStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        item.status = SeasonalClearanceNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalClearanceNoticeStatus.REQUESTED) }
    }
}

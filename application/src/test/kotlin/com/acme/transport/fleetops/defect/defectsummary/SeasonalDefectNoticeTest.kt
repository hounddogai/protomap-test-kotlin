package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDefectNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalDefectNotice("code-96", "name-185")
        assertEquals(SeasonalDefectNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalDefectNotice("code-96", "name-185")
        try {
            item.moveTo(SeasonalDefectNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDefectNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalDefectNotice("code-96", "name-185")
        item.status = SeasonalDefectNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalDefectNoticeStatus.PROPOSED) }
    }
}

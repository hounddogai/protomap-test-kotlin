package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServicePlanRecordTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        assertEquals(SeasonalServicePlanRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        try {
            item.moveTo(SeasonalServicePlanRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServicePlanRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        item.status = SeasonalServicePlanRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalServicePlanRecordStatus.OPEN) }
    }
}

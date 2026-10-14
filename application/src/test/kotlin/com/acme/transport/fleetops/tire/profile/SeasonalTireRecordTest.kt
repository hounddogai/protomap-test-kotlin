package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTireRecordTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalTireRecord("code-988", "label-4")
        assertEquals(SeasonalTireRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalTireRecord("code-988", "label-4")
        try {
            item.moveTo(SeasonalTireRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTireRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalTireRecord("code-988", "label-4")
        item.status = SeasonalTireRecordStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalTireRecordStatus.NEW) }
    }
}

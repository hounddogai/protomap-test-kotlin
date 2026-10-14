package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalParcelRecordTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalParcelRecord("code-568", "label-124")
        assertEquals(SeasonalParcelRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalParcelRecord("code-568", "label-124")
        try {
            item.moveTo(SeasonalParcelRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalParcelRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalParcelRecord("code-568", "label-124")
        item.status = SeasonalParcelRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalParcelRecordStatus.OPEN) }
    }
}

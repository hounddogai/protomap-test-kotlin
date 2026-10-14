package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalIdlingWindowTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalIdlingWindow("code-420", "label-748")
        assertEquals(SeasonalIdlingWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalIdlingWindow("code-420", "label-748")
        try {
            item.moveTo(SeasonalIdlingWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalIdlingWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalIdlingWindow("code-420", "label-748")
        item.status = SeasonalIdlingWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalIdlingWindowStatus.OPEN) }
    }
}

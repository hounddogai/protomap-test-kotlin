package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalParcelEntry("code-901", "label-49")
        assertEquals(RegionalParcelEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalParcelEntry("code-901", "label-49")
        try {
            item.moveTo(RegionalParcelEntryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelEntryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalParcelEntry("code-901", "label-49")
        item.status = RegionalParcelEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalParcelEntryStatus.PLANNED) }
    }
}

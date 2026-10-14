package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeEntryTest {
    @Test
    fun `starts in created`() {
        val item = newTransitTimeEntry("code-473", "title-654")
        assertEquals(TransitTimeEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTransitTimeEntry("code-473", "title-654")
        try {
            item.moveTo(TransitTimeEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTransitTimeEntry("code-473", "title-654")
        item.status = TransitTimeEntryStatus.RETIRED
        assertFails("move") { item.moveTo(TransitTimeEntryStatus.CREATED) }
    }
}

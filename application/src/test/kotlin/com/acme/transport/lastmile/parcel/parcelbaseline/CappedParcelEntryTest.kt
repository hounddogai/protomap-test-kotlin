package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedParcelEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedParcelEntry("code-463", "name-976")
        assertEquals(CappedParcelEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedParcelEntry("code-463", "name-976")
        try {
            item.moveTo(CappedParcelEntryStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedParcelEntryStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedParcelEntry("code-463", "name-976")
        item.status = CappedParcelEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedParcelEntryStatus.PROPOSED) }
    }
}

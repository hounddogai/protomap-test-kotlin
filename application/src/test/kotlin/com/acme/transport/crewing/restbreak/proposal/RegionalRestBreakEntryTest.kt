package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRestBreakEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalRestBreakEntry("code-728", "name-475")
        assertEquals(RegionalRestBreakEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalRestBreakEntry("code-728", "name-475")
        try {
            item.moveTo(RegionalRestBreakEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRestBreakEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalRestBreakEntry("code-728", "name-475")
        item.status = RegionalRestBreakEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalRestBreakEntryStatus.REQUESTED) }
    }
}

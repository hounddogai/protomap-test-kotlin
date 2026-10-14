package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRouteOffsetEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalRouteOffsetEntry("code-837", "name-470")
        assertEquals(RegionalRouteOffsetEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalRouteOffsetEntry("code-837", "name-470")
        try {
            item.moveTo(RegionalRouteOffsetEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRouteOffsetEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalRouteOffsetEntry("code-837", "name-470")
        item.status = RegionalRouteOffsetEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalRouteOffsetEntryStatus.REQUESTED) }
    }
}

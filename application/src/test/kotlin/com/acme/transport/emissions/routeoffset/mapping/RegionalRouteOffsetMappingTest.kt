package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRouteOffsetMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalRouteOffsetMapping("code-319", "title-831")
        assertEquals(RegionalRouteOffsetMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalRouteOffsetMapping("code-319", "title-831")
        try {
            item.moveTo(RegionalRouteOffsetMappingStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRouteOffsetMappingStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalRouteOffsetMapping("code-319", "title-831")
        item.status = RegionalRouteOffsetMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalRouteOffsetMappingStatus.REQUESTED) }
    }
}

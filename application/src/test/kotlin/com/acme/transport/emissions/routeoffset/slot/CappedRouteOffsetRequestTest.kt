package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRouteOffsetRequestTest {
    @Test
    fun `starts in open`() {
        val item = newCappedRouteOffsetRequest("code-907", "name-97")
        assertEquals(CappedRouteOffsetRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedRouteOffsetRequest("code-907", "name-97")
        try {
            item.moveTo(CappedRouteOffsetRequestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRouteOffsetRequestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedRouteOffsetRequest("code-907", "name-97")
        item.status = CappedRouteOffsetRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedRouteOffsetRequestStatus.OPEN) }
    }
}

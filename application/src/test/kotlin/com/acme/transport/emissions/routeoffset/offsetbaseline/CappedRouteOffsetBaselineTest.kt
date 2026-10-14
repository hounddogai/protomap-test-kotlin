package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRouteOffsetBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedRouteOffsetBaseline("code-183", "name-853")
        assertEquals(CappedRouteOffsetBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedRouteOffsetBaseline("code-183", "name-853")
        try {
            item.moveTo(CappedRouteOffsetBaselineStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRouteOffsetBaselineStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedRouteOffsetBaseline("code-183", "name-853")
        item.status = CappedRouteOffsetBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedRouteOffsetBaselineStatus.PROPOSED) }
    }
}

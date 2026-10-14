package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newRouteOffsetAdjustment("code-735", "title-518")
        assertEquals(RouteOffsetAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRouteOffsetAdjustment("code-735", "title-518")
        try {
            item.moveTo(RouteOffsetAdjustmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetAdjustmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRouteOffsetAdjustment("code-735", "title-518")
        item.status = RouteOffsetAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(RouteOffsetAdjustmentStatus.NEW) }
    }
}

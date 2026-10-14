package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newRouteOffsetThreshold("code-274", "label-479")
        assertEquals(RouteOffsetThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRouteOffsetThreshold("code-274", "label-479")
        try {
            item.moveTo(RouteOffsetThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRouteOffsetThreshold("code-274", "label-479")
        item.status = RouteOffsetThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RouteOffsetThresholdStatus.PROPOSED) }
    }
}

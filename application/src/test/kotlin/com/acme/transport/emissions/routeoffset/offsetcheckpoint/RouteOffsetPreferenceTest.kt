package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetPreference("code-579", "label-453")
        assertEquals(RouteOffsetPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRouteOffsetPreference("code-579", "label-453")
        try {
            item.moveTo(RouteOffsetPreferenceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetPreferenceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetPreference("code-579", "label-453")
        item.status = RouteOffsetPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetPreferenceStatus.IDLE) }
    }
}

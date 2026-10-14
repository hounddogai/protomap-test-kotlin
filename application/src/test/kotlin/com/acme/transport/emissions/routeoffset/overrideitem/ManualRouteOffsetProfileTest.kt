package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRouteOffsetProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newManualRouteOffsetProfile("code-447", "name-464")
        assertEquals(ManualRouteOffsetProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualRouteOffsetProfile("code-447", "name-464")
        try {
            item.moveTo(ManualRouteOffsetProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRouteOffsetProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualRouteOffsetProfile("code-447", "name-464")
        item.status = ManualRouteOffsetProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualRouteOffsetProfileStatus.PLANNED) }
    }
}

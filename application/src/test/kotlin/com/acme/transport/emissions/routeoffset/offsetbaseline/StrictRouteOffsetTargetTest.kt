package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRouteOffsetTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictRouteOffsetTarget("code-895", "label-801")
        assertEquals(StrictRouteOffsetTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictRouteOffsetTarget("code-895", "label-801")
        try {
            item.moveTo(StrictRouteOffsetTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRouteOffsetTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictRouteOffsetTarget("code-895", "label-801")
        item.status = StrictRouteOffsetTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictRouteOffsetTargetStatus.PLANNED) }
    }
}

package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackRouteOffsetSchedule("code-190", "name-952")
        assertEquals(FallbackRouteOffsetScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackRouteOffsetSchedule("code-190", "name-952")
        try {
            item.moveTo(FallbackRouteOffsetScheduleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetScheduleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackRouteOffsetSchedule("code-190", "name-952")
        item.status = FallbackRouteOffsetScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackRouteOffsetScheduleStatus.OPEN) }
    }
}

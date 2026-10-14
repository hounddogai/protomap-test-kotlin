package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerPlanTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerPlan("code-915", "title-667")
        assertEquals(TrailerPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTrailerPlan("code-915", "title-667")
        try {
            item.moveTo(TrailerPlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerPlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerPlan("code-915", "title-667")
        item.status = TrailerPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerPlanStatus.OPEN) }
    }
}

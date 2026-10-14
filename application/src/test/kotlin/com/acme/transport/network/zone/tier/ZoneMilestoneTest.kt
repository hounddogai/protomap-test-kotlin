package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newZoneMilestone("code-891", "title-166")
        assertEquals(ZoneMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newZoneMilestone("code-891", "title-166")
        try {
            item.moveTo(ZoneMilestoneStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneMilestoneStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newZoneMilestone("code-891", "title-166")
        item.status = ZoneMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ZoneMilestoneStatus.PROPOSED) }
    }
}

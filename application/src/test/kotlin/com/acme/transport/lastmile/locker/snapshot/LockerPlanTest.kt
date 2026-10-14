package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerPlanTest {
    @Test
    fun `starts in open`() {
        val item = newLockerPlan("code-704", "title-943")
        assertEquals(LockerPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLockerPlan("code-704", "title-943")
        try {
            item.moveTo(LockerPlanStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerPlanStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLockerPlan("code-704", "title-943")
        item.status = LockerPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(LockerPlanStatus.OPEN) }
    }
}

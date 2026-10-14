package com.acme.transport.lastmile.locker.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLockerPlanTest {
    @Test
    fun `starts in new`() {
        val item = newRollingLockerPlan("code-497", "title-766")
        assertEquals(RollingLockerPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRollingLockerPlan("code-497", "title-766")
        try {
            item.moveTo(RollingLockerPlanStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLockerPlanStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingLockerPlan("code-497", "title-766")
        item.status = RollingLockerPlanStatus.FAILED
        assertFails("move") { item.moveTo(RollingLockerPlanStatus.NEW) }
    }
}

package com.acme.transport.lastmile.locker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLockerPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultLockerPlan("code-574", "title-711")
        assertEquals(DefaultLockerPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultLockerPlan("code-574", "title-711")
        try {
            item.moveTo(DefaultLockerPlanStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLockerPlanStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultLockerPlan("code-574", "title-711")
        item.status = DefaultLockerPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultLockerPlanStatus.REQUESTED) }
    }
}

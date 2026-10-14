package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLockerPlanTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackLockerPlan("code-724", "title-742")
        assertEquals(FallbackLockerPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackLockerPlan("code-724", "title-742")
        try {
            item.moveTo(FallbackLockerPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLockerPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackLockerPlan("code-724", "title-742")
        item.status = FallbackLockerPlanStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackLockerPlanStatus.CREATED) }
    }
}

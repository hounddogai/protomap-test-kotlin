package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLockerCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedLockerCycle("code-221", "name-735")
        assertEquals(WeightedLockerCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedLockerCycle("code-221", "name-735")
        try {
            item.moveTo(WeightedLockerCycleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLockerCycleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedLockerCycle("code-221", "name-735")
        item.status = WeightedLockerCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedLockerCycleStatus.PLANNED) }
    }
}

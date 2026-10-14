package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLockerProfileTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedLockerProfile("code-891", "title-413")
        assertEquals(WeightedLockerProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedLockerProfile("code-891", "title-413")
        try {
            item.moveTo(WeightedLockerProfileStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLockerProfileStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedLockerProfile("code-891", "title-413")
        item.status = WeightedLockerProfileStatus.FAILED
        assertFails("move") { item.moveTo(WeightedLockerProfileStatus.NEW) }
    }
}

package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newLockerEstimate("code-40", "label-515")
        assertEquals(LockerEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerEstimate("code-40", "label-515")
        try {
            item.moveTo(LockerEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerEstimate("code-40", "label-515")
        item.status = LockerEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(LockerEstimateStatus.CREATED) }
    }
}

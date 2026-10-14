package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLockerProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalLockerProfile("code-866", "name-380")
        assertEquals(RegionalLockerProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalLockerProfile("code-866", "name-380")
        try {
            item.moveTo(RegionalLockerProfileStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLockerProfileStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalLockerProfile("code-866", "name-380")
        item.status = RegionalLockerProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalLockerProfileStatus.PLANNED) }
    }
}

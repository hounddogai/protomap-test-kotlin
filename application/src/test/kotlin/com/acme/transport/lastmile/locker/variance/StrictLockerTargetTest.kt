package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLockerTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictLockerTarget("code-294", "title-993")
        assertEquals(StrictLockerTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictLockerTarget("code-294", "title-993")
        try {
            item.moveTo(StrictLockerTargetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLockerTargetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictLockerTarget("code-294", "title-993")
        item.status = StrictLockerTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictLockerTargetStatus.PLANNED) }
    }
}

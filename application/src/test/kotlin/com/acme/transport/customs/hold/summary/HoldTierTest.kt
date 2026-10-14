package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldTierTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldTier("code-88", "title-654")
        assertEquals(HoldTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newHoldTier("code-88", "title-654")
        try {
            item.moveTo(HoldTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldTier("code-88", "title-654")
        item.status = HoldTierStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldTierStatus.PLANNED) }
    }
}

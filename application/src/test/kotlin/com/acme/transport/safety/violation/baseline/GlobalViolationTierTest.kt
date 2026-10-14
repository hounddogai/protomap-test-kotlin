package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalViolationTierTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalViolationTier("code-45", "name-105")
        assertEquals(GlobalViolationTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newGlobalViolationTier("code-45", "name-105")
        try {
            item.moveTo(GlobalViolationTierStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalViolationTierStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalViolationTier("code-45", "name-105")
        item.status = GlobalViolationTierStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalViolationTierStatus.PLANNED) }
    }
}

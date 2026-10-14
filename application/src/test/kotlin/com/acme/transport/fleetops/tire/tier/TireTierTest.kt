package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireTierTest {
    @Test
    fun `starts in planned`() {
        val item = newTireTier("code-10", "name-703")
        assertEquals(TireTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTireTier("code-10", "name-703")
        try {
            item.moveTo(TireTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireTier("code-10", "name-703")
        item.status = TireTierStatus.CANCELLED
        assertFails("move") { item.moveTo(TireTierStatus.PLANNED) }
    }
}

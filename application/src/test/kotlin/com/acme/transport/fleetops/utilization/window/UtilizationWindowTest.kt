package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationWindow("code-785", "name-862")
        assertEquals(UtilizationWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newUtilizationWindow("code-785", "name-862")
        try {
            item.moveTo(UtilizationWindowStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationWindowStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationWindow("code-785", "name-862")
        item.status = UtilizationWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationWindowStatus.PLANNED) }
    }
}

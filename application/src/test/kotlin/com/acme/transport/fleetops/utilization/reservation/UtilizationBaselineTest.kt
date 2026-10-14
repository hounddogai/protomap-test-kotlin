package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newUtilizationBaseline("code-976", "name-435")
        assertEquals(UtilizationBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newUtilizationBaseline("code-976", "name-435")
        try {
            item.moveTo(UtilizationBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newUtilizationBaseline("code-976", "name-435")
        item.status = UtilizationBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(UtilizationBaselineStatus.REQUESTED) }
    }
}

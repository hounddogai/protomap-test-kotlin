package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationTierTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationTier("code-861", "label-569")
        assertEquals(UtilizationTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newUtilizationTier("code-861", "label-569")
        try {
            item.moveTo(UtilizationTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationTier("code-861", "label-569")
        item.status = UtilizationTierStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationTierStatus.PLANNED) }
    }
}

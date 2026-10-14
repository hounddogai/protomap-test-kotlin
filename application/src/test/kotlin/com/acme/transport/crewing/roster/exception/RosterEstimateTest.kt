package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterEstimate("code-206", "label-485")
        assertEquals(RosterEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRosterEstimate("code-206", "label-485")
        try {
            item.moveTo(RosterEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterEstimate("code-206", "label-485")
        item.status = RosterEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterEstimateStatus.REQUESTED) }
    }
}

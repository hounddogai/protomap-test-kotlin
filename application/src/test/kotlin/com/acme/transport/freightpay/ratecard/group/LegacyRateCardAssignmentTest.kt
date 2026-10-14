package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRateCardAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyRateCardAssignment("code-370", "name-634")
        assertEquals(LegacyRateCardAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyRateCardAssignment("code-370", "name-634")
        try {
            item.moveTo(LegacyRateCardAssignmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRateCardAssignmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyRateCardAssignment("code-370", "name-634")
        item.status = LegacyRateCardAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(LegacyRateCardAssignmentStatus.NEW) }
    }
}

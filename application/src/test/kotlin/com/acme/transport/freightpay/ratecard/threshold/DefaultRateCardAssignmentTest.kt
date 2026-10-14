package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRateCardAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultRateCardAssignment("code-560", "name-310")
        assertEquals(DefaultRateCardAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultRateCardAssignment("code-560", "name-310")
        try {
            item.moveTo(DefaultRateCardAssignmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRateCardAssignmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultRateCardAssignment("code-560", "name-310")
        item.status = DefaultRateCardAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(DefaultRateCardAssignmentStatus.NEW) }
    }
}

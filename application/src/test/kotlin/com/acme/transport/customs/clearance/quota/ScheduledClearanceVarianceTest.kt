package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledClearanceVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledClearanceVariance("code-320", "title-803")
        assertEquals(ScheduledClearanceVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledClearanceVariance("code-320", "title-803")
        try {
            item.moveTo(ScheduledClearanceVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledClearanceVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledClearanceVariance("code-320", "title-803")
        item.status = ScheduledClearanceVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledClearanceVarianceStatus.PLANNED) }
    }
}

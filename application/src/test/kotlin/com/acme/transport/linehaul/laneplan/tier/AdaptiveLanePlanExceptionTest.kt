package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveLanePlanException("code-206", "title-636")
        assertEquals(AdaptiveLanePlanExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveLanePlanException("code-206", "title-636")
        try {
            item.moveTo(AdaptiveLanePlanExceptionStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanExceptionStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveLanePlanException("code-206", "title-636")
        item.status = AdaptiveLanePlanExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveLanePlanExceptionStatus.PLANNED) }
    }
}

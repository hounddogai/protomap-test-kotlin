package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newLanePlanRequest("code-125", "label-467")
        assertEquals(LanePlanRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLanePlanRequest("code-125", "label-467")
        try {
            item.moveTo(LanePlanRequestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanRequestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLanePlanRequest("code-125", "label-467")
        item.status = LanePlanRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(LanePlanRequestStatus.PLANNED) }
    }
}

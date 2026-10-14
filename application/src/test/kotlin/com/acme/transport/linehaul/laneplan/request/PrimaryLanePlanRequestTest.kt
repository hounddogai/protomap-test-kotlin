package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLanePlanRequestTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryLanePlanRequest("code-844", "name-636")
        assertEquals(PrimaryLanePlanRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPrimaryLanePlanRequest("code-844", "name-636")
        try {
            item.moveTo(PrimaryLanePlanRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLanePlanRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryLanePlanRequest("code-844", "name-636")
        item.status = PrimaryLanePlanRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryLanePlanRequestStatus.OPEN) }
    }
}

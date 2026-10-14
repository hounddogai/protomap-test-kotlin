package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIncidentRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalIncidentRequest("code-318", "title-15")
        assertEquals(GlobalIncidentRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalIncidentRequest("code-318", "title-15")
        try {
            item.moveTo(GlobalIncidentRequestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIncidentRequestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalIncidentRequest("code-318", "title-15")
        item.status = GlobalIncidentRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalIncidentRequestStatus.REQUESTED) }
    }
}

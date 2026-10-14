package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyIncidentRequest("code-432", "title-775")
        assertEquals(LegacyIncidentRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyIncidentRequest("code-432", "title-775")
        try {
            item.moveTo(LegacyIncidentRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyIncidentRequest("code-432", "title-775")
        item.status = LegacyIncidentRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyIncidentRequestStatus.REQUESTED) }
    }
}

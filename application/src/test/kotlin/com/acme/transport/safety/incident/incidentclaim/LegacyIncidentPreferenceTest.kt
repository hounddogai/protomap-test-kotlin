package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyIncidentPreference("code-163", "label-14")
        assertEquals(LegacyIncidentPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLegacyIncidentPreference("code-163", "label-14")
        try {
            item.moveTo(LegacyIncidentPreferenceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentPreferenceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyIncidentPreference("code-163", "label-14")
        item.status = LegacyIncidentPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyIncidentPreferenceStatus.DRAFT) }
    }
}

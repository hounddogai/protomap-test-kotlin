package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentTierTest {
    @Test
    fun `starts in open`() {
        val item = newIncidentTier("code-285", "label-178")
        assertEquals(IncidentTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newIncidentTier("code-285", "label-178")
        try {
            item.moveTo(IncidentTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIncidentTier("code-285", "label-178")
        item.status = IncidentTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(IncidentTierStatus.OPEN) }
    }
}

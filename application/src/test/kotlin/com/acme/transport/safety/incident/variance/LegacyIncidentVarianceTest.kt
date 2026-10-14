package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyIncidentVariance("code-164", "label-645")
        assertEquals(LegacyIncidentVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLegacyIncidentVariance("code-164", "label-645")
        try {
            item.moveTo(LegacyIncidentVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyIncidentVariance("code-164", "label-645")
        item.status = LegacyIncidentVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyIncidentVarianceStatus.OPEN) }
    }
}

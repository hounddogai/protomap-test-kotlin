package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newIncidentVariance("code-901", "name-482")
        assertEquals(IncidentVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newIncidentVariance("code-901", "name-482")
        try {
            item.moveTo(IncidentVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIncidentVariance("code-901", "name-482")
        item.status = IncidentVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(IncidentVarianceStatus.PLANNED) }
    }
}

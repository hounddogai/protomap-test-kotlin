package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentEstimate("code-700", "title-111")
        assertEquals(IncidentEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newIncidentEstimate("code-700", "title-111")
        try {
            item.moveTo(IncidentEstimateStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentEstimateStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentEstimate("code-700", "title-111")
        item.status = IncidentEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentEstimateStatus.PROPOSED) }
    }
}

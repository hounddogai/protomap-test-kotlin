package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIncidentReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalIncidentReview("code-617", "name-618")
        assertEquals(RegionalIncidentReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRegionalIncidentReview("code-617", "name-618")
        try {
            item.moveTo(RegionalIncidentReviewStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIncidentReviewStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalIncidentReview("code-617", "name-618")
        item.status = RegionalIncidentReviewStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalIncidentReviewStatus.IDLE) }
    }
}

package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newIncidentReview("code-977", "name-392")
        assertEquals(IncidentReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIncidentReview("code-977", "name-392")
        try {
            item.moveTo(IncidentReviewStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentReviewStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIncidentReview("code-977", "name-392")
        item.status = IncidentReviewStatus.CLOSED
        assertFails("move") { item.moveTo(IncidentReviewStatus.DRAFT) }
    }
}

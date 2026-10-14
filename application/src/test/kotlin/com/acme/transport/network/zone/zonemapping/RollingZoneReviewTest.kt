package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingZoneReviewTest {
    @Test
    fun `starts in open`() {
        val item = newRollingZoneReview("code-399", "title-404")
        assertEquals(RollingZoneReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRollingZoneReview("code-399", "title-404")
        try {
            item.moveTo(RollingZoneReviewStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingZoneReviewStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingZoneReview("code-399", "title-404")
        item.status = RollingZoneReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingZoneReviewStatus.OPEN) }
    }
}

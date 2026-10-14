package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedHubAuditTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedHubAudit("code-621", "title-327")
        assertEquals(WeightedHubAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedHubAudit("code-621", "title-327")
        try {
            item.moveTo(WeightedHubAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedHubAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedHubAudit("code-621", "title-327")
        item.status = WeightedHubAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedHubAuditStatus.OPEN) }
    }
}

package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeScoreTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeScore("code-373", "title-284")
        assertEquals(DisputeScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDisputeScore("code-373", "title-284")
        try {
            item.moveTo(DisputeScoreStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeScoreStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeScore("code-373", "title-284")
        item.status = DisputeScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeScoreStatus.OPEN) }
    }
}

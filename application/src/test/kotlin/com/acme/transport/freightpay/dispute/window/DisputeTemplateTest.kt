package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newDisputeTemplate("code-527", "name-712")
        assertEquals(DisputeTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDisputeTemplate("code-527", "name-712")
        try {
            item.moveTo(DisputeTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDisputeTemplate("code-527", "name-712")
        item.status = DisputeTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(DisputeTemplateStatus.PLANNED) }
    }
}

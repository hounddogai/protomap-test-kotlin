package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeWindowTest {
    @Test
    fun `starts in created`() {
        val item = newDisputeWindow("code-168", "title-747")
        assertEquals(DisputeWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDisputeWindow("code-168", "title-747")
        try {
            item.moveTo(DisputeWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDisputeWindow("code-168", "title-747")
        item.status = DisputeWindowStatus.RETIRED
        assertFails("move") { item.moveTo(DisputeWindowStatus.CREATED) }
    }
}

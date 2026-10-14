package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeBaseline("code-81", "label-297")
        assertEquals(DisputeBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDisputeBaseline("code-81", "label-297")
        try {
            item.moveTo(DisputeBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeBaseline("code-81", "label-297")
        item.status = DisputeBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeBaselineStatus.REQUESTED) }
    }
}

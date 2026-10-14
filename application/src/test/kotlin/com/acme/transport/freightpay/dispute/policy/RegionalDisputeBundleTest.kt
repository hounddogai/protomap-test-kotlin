package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDisputeBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalDisputeBundle("code-987", "name-162")
        assertEquals(RegionalDisputeBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalDisputeBundle("code-987", "name-162")
        try {
            item.moveTo(RegionalDisputeBundleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDisputeBundleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalDisputeBundle("code-987", "name-162")
        item.status = RegionalDisputeBundleStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalDisputeBundleStatus.DRAFT) }
    }
}

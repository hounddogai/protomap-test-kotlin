package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRedeliveryClaimTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalRedeliveryClaim("code-235", "label-387")
        assertEquals(RegionalRedeliveryClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalRedeliveryClaim("code-235", "label-387")
        try {
            item.moveTo(RegionalRedeliveryClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRedeliveryClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalRedeliveryClaim("code-235", "label-387")
        item.status = RegionalRedeliveryClaimStatus.FAILED
        assertFails("move") { item.moveTo(RegionalRedeliveryClaimStatus.NEW) }
    }
}

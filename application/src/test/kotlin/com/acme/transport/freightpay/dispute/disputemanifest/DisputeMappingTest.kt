package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeMappingTest {
    @Test
    fun `starts in created`() {
        val item = newDisputeMapping("code-454", "name-425")
        assertEquals(DisputeMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDisputeMapping("code-454", "name-425")
        try {
            item.moveTo(DisputeMappingStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeMappingStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDisputeMapping("code-454", "name-425")
        item.status = DisputeMappingStatus.RETIRED
        assertFails("move") { item.moveTo(DisputeMappingStatus.CREATED) }
    }
}

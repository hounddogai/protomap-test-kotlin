package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDisputeProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalDisputeProfile("code-773", "title-802")
        assertEquals(GlobalDisputeProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalDisputeProfile("code-773", "title-802")
        try {
            item.moveTo(GlobalDisputeProfileStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDisputeProfileStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalDisputeProfile("code-773", "title-802")
        item.status = GlobalDisputeProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalDisputeProfileStatus.REQUESTED) }
    }
}

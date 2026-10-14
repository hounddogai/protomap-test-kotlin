package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTariffClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingTariffClaim("code-661", "label-703")
        assertEquals(PendingTariffClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingTariffClaim("code-661", "label-703")
        try {
            item.moveTo(PendingTariffClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTariffClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingTariffClaim("code-661", "label-703")
        item.status = PendingTariffClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingTariffClaimStatus.PROPOSED) }
    }
}

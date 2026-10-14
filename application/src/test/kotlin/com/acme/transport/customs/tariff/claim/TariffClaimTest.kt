package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newTariffClaim("code-788", "label-435")
        assertEquals(TariffClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTariffClaim("code-788", "label-435")
        try {
            item.moveTo(TariffClaimStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffClaimStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTariffClaim("code-788", "label-435")
        item.status = TariffClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(TariffClaimStatus.PLANNED) }
    }
}

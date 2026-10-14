package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTariffClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedTariffClaim("code-72", "name-676")
        assertEquals(CappedTariffClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedTariffClaim("code-72", "name-676")
        try {
            item.moveTo(CappedTariffClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTariffClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedTariffClaim("code-72", "name-676")
        item.status = CappedTariffClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedTariffClaimStatus.REQUESTED) }
    }
}

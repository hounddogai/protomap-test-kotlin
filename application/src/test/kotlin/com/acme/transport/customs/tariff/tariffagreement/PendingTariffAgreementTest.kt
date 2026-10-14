package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTariffAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingTariffAgreement("code-336", "title-185")
        assertEquals(PendingTariffAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingTariffAgreement("code-336", "title-185")
        try {
            item.moveTo(PendingTariffAgreementStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTariffAgreementStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingTariffAgreement("code-336", "title-185")
        item.status = PendingTariffAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingTariffAgreementStatus.PROPOSED) }
    }
}

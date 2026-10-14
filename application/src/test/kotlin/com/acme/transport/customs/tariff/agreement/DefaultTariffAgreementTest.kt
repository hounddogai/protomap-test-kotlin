package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultTariffAgreement("code-607", "name-747")
        assertEquals(DefaultTariffAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefaultTariffAgreement("code-607", "name-747")
        try {
            item.moveTo(DefaultTariffAgreementStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffAgreementStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultTariffAgreement("code-607", "name-747")
        item.status = DefaultTariffAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultTariffAgreementStatus.DRAFT) }
    }
}

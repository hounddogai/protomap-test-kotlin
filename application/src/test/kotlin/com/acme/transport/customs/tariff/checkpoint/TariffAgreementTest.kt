package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newTariffAgreement("code-639", "label-80")
        assertEquals(TariffAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTariffAgreement("code-639", "label-80")
        try {
            item.moveTo(TariffAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffAgreement("code-639", "label-80")
        item.status = TariffAgreementStatus.FAILED
        assertFails("move") { item.moveTo(TariffAgreementStatus.NEW) }
    }
}

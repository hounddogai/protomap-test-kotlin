package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newEvChargeAgreement("code-932", "title-270")
        assertEquals(EvChargeAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newEvChargeAgreement("code-932", "title-270")
        try {
            item.moveTo(EvChargeAgreementStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeAgreementStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEvChargeAgreement("code-932", "title-270")
        item.status = EvChargeAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(EvChargeAgreementStatus.CREATED) }
    }
}

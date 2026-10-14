package com.acme.transport.linehaul.capacity.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        assertEquals(RegionalCapacityAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        try {
            item.moveTo(RegionalCapacityAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        item.status = RegionalCapacityAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalCapacityAgreementStatus.DRAFT) }
    }
}

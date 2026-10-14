package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newUtilizationAgreement("code-352", "title-38")
        assertEquals(UtilizationAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newUtilizationAgreement("code-352", "title-38")
        try {
            item.moveTo(UtilizationAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newUtilizationAgreement("code-352", "title-38")
        item.status = UtilizationAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(UtilizationAgreementStatus.REQUESTED) }
    }
}

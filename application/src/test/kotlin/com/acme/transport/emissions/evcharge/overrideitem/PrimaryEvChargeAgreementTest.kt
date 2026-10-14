package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        assertEquals(PrimaryEvChargeAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        try {
            item.moveTo(PrimaryEvChargeAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        item.status = PrimaryEvChargeAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryEvChargeAgreementStatus.IDLE) }
    }
}

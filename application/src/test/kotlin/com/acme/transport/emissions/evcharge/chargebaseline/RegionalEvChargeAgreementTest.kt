package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalEvChargeAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalEvChargeAgreement("code-545", "label-832")
        assertEquals(RegionalEvChargeAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRegionalEvChargeAgreement("code-545", "label-832")
        try {
            item.moveTo(RegionalEvChargeAgreementStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalEvChargeAgreementStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalEvChargeAgreement("code-545", "label-832")
        item.status = RegionalEvChargeAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalEvChargeAgreementStatus.IDLE) }
    }
}

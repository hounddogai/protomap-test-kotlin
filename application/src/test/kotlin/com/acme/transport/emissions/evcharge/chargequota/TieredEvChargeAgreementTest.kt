package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEvChargeAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredEvChargeAgreement("code-322", "name-171")
        assertEquals(TieredEvChargeAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredEvChargeAgreement("code-322", "name-171")
        try {
            item.moveTo(TieredEvChargeAgreementStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEvChargeAgreementStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredEvChargeAgreement("code-322", "name-171")
        item.status = TieredEvChargeAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(TieredEvChargeAgreementStatus.IDLE) }
    }
}

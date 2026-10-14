package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelBurnAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalFuelBurnAgreement("code-230", "label-572")
        assertEquals(GlobalFuelBurnAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newGlobalFuelBurnAgreement("code-230", "label-572")
        try {
            item.moveTo(GlobalFuelBurnAgreementStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelBurnAgreementStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalFuelBurnAgreement("code-230", "label-572")
        item.status = GlobalFuelBurnAgreementStatus.FAILED
        assertFails("move") { item.moveTo(GlobalFuelBurnAgreementStatus.NEW) }
    }
}

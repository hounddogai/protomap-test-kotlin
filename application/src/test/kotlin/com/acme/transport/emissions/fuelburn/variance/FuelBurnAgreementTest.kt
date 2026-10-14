package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelBurnAgreement("code-882", "name-115")
        assertEquals(FuelBurnAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFuelBurnAgreement("code-882", "name-115")
        try {
            item.moveTo(FuelBurnAgreementStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnAgreementStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelBurnAgreement("code-882", "name-115")
        item.status = FuelBurnAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(FuelBurnAgreementStatus.DRAFT) }
    }
}

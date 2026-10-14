package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargePreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargePreference("code-145", "title-203")
        assertEquals(FuelSurchargePreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFuelSurchargePreference("code-145", "title-203")
        try {
            item.moveTo(FuelSurchargePreferenceStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargePreferenceStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargePreference("code-145", "title-203")
        item.status = FuelSurchargePreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargePreferenceStatus.DRAFT) }
    }
}

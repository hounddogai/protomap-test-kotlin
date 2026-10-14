package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeBundleTest {
    @Test
    fun `starts in created`() {
        val item = newFuelSurchargeBundle("code-94", "name-752")
        assertEquals(FuelSurchargeBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelSurchargeBundle("code-94", "name-752")
        try {
            item.moveTo(FuelSurchargeBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelSurchargeBundle("code-94", "name-752")
        item.status = FuelSurchargeBundleStatus.RETIRED
        assertFails("move") { item.moveTo(FuelSurchargeBundleStatus.CREATED) }
    }
}

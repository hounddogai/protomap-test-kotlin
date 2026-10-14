package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelSurchargeBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientFuelSurchargeBundle("code-634", "title-318")
        assertEquals(LenientFuelSurchargeBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientFuelSurchargeBundle("code-634", "title-318")
        try {
            item.moveTo(LenientFuelSurchargeBundleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelSurchargeBundleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientFuelSurchargeBundle("code-634", "title-318")
        item.status = LenientFuelSurchargeBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientFuelSurchargeBundleStatus.REQUESTED) }
    }
}

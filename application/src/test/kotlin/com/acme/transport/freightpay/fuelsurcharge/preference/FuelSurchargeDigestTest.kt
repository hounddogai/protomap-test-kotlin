package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeDigestTest {
    @Test
    fun `starts in created`() {
        val item = newFuelSurchargeDigest("code-72", "title-675")
        assertEquals(FuelSurchargeDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelSurchargeDigest("code-72", "title-675")
        try {
            item.moveTo(FuelSurchargeDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelSurchargeDigest("code-72", "title-675")
        item.status = FuelSurchargeDigestStatus.RETIRED
        assertFails("move") { item.moveTo(FuelSurchargeDigestStatus.CREATED) }
    }
}

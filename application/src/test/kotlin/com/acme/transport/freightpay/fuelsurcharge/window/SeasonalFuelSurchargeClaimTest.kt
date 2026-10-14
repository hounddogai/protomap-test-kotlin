package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargeClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalFuelSurchargeClaim("code-464", "label-901")
        assertEquals(SeasonalFuelSurchargeClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalFuelSurchargeClaim("code-464", "label-901")
        try {
            item.moveTo(SeasonalFuelSurchargeClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargeClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalFuelSurchargeClaim("code-464", "label-901")
        item.status = SeasonalFuelSurchargeClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalFuelSurchargeClaimStatus.REQUESTED) }
    }
}

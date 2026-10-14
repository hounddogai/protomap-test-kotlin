package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargeSlotTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        assertEquals(SeasonalFuelSurchargeSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        try {
            item.moveTo(SeasonalFuelSurchargeSlotStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargeSlotStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        item.status = SeasonalFuelSurchargeSlotStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalFuelSurchargeSlotStatus.NEW) }
    }
}

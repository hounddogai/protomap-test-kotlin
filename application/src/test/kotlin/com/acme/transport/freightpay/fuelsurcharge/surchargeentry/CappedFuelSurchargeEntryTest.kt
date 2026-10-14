package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedFuelSurchargeEntryTest {
    @Test
    fun `starts in new`() {
        val item = newCappedFuelSurchargeEntry("code-569", "label-235")
        assertEquals(CappedFuelSurchargeEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCappedFuelSurchargeEntry("code-569", "label-235")
        try {
            item.moveTo(CappedFuelSurchargeEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedFuelSurchargeEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedFuelSurchargeEntry("code-569", "label-235")
        item.status = CappedFuelSurchargeEntryStatus.FAILED
        assertFails("move") { item.moveTo(CappedFuelSurchargeEntryStatus.NEW) }
    }
}

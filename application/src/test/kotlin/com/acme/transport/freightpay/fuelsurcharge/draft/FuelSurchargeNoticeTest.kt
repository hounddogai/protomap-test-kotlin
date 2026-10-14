package com.acme.transport.freightpay.fuelsurcharge.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelSurchargeNotice("code-738", "title-464")
        assertEquals(FuelSurchargeNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFuelSurchargeNotice("code-738", "title-464")
        try {
            item.moveTo(FuelSurchargeNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelSurchargeNotice("code-738", "title-464")
        item.status = FuelSurchargeNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelSurchargeNoticeStatus.PLANNED) }
    }
}

package com.acme.transport.freightpay.fuelsurcharge.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFuelSurchargeGroupTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyFuelSurchargeGroup("code-109", "label-12")
        assertEquals(LegacyFuelSurchargeGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyFuelSurchargeGroup("code-109", "label-12")
        try {
            item.moveTo(LegacyFuelSurchargeGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFuelSurchargeGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyFuelSurchargeGroup("code-109", "label-12")
        item.status = LegacyFuelSurchargeGroupStatus.FAILED
        assertFails("move") { item.moveTo(LegacyFuelSurchargeGroupStatus.NEW) }
    }
}

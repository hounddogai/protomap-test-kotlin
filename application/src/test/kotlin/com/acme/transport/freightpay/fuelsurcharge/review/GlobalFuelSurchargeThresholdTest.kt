package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelSurchargeThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalFuelSurchargeThreshold("code-896", "title-42")
        assertEquals(GlobalFuelSurchargeThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalFuelSurchargeThreshold("code-896", "title-42")
        try {
            item.moveTo(GlobalFuelSurchargeThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelSurchargeThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalFuelSurchargeThreshold("code-896", "title-42")
        item.status = GlobalFuelSurchargeThresholdStatus.FAILED
        assertFails("move") { item.moveTo(GlobalFuelSurchargeThresholdStatus.NEW) }
    }
}

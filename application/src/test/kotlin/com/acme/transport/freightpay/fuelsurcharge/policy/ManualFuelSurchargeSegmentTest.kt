package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelSurchargeSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        assertEquals(ManualFuelSurchargeSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        try {
            item.moveTo(ManualFuelSurchargeSegmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelSurchargeSegmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        item.status = ManualFuelSurchargeSegmentStatus.FAILED
        assertFails("move") { item.moveTo(ManualFuelSurchargeSegmentStatus.NEW) }
    }
}

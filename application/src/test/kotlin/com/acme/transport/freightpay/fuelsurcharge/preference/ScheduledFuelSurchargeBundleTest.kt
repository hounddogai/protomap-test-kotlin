package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelSurchargeBundleTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledFuelSurchargeBundle("code-901", "name-583")
        assertEquals(ScheduledFuelSurchargeBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledFuelSurchargeBundle("code-901", "name-583")
        try {
            item.moveTo(ScheduledFuelSurchargeBundleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelSurchargeBundleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledFuelSurchargeBundle("code-901", "name-583")
        item.status = ScheduledFuelSurchargeBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledFuelSurchargeBundleStatus.OPEN) }
    }
}

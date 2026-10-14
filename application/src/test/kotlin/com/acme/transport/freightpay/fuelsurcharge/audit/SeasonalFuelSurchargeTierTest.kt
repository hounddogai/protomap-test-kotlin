package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargeTierTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalFuelSurchargeTier("code-865", "label-486")
        assertEquals(SeasonalFuelSurchargeTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSeasonalFuelSurchargeTier("code-865", "label-486")
        try {
            item.moveTo(SeasonalFuelSurchargeTierStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargeTierStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalFuelSurchargeTier("code-865", "label-486")
        item.status = SeasonalFuelSurchargeTierStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalFuelSurchargeTierStatus.IDLE) }
    }
}

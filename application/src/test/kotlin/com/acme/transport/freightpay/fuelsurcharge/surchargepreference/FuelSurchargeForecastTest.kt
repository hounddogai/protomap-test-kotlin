package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeForecast("code-295", "name-728")
        assertEquals(FuelSurchargeForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFuelSurchargeForecast("code-295", "name-728")
        try {
            item.moveTo(FuelSurchargeForecastStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeForecastStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeForecast("code-295", "name-728")
        item.status = FuelSurchargeForecastStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeForecastStatus.IDLE) }
    }
}

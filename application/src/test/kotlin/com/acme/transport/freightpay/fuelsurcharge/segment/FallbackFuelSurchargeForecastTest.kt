package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackFuelSurchargeForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackFuelSurchargeForecast("code-457", "name-748")
        assertEquals(FallbackFuelSurchargeForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackFuelSurchargeForecast("code-457", "name-748")
        try {
            item.moveTo(FallbackFuelSurchargeForecastStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackFuelSurchargeForecastStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackFuelSurchargeForecast("code-457", "name-748")
        item.status = FallbackFuelSurchargeForecastStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackFuelSurchargeForecastStatus.IDLE) }
    }
}

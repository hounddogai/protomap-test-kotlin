package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelSurchargeWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledFuelSurchargeWindow("code-894", "name-875")
        assertEquals(ScheduledFuelSurchargeWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScheduledFuelSurchargeWindow("code-894", "name-875")
        try {
            item.moveTo(ScheduledFuelSurchargeWindowStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelSurchargeWindowStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledFuelSurchargeWindow("code-894", "name-875")
        item.status = ScheduledFuelSurchargeWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledFuelSurchargeWindowStatus.PROPOSED) }
    }
}

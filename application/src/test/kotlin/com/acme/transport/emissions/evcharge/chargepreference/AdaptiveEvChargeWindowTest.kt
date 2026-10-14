package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEvChargeWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveEvChargeWindow("code-474", "name-632")
        assertEquals(AdaptiveEvChargeWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newAdaptiveEvChargeWindow("code-474", "name-632")
        try {
            item.moveTo(AdaptiveEvChargeWindowStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEvChargeWindowStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveEvChargeWindow("code-474", "name-632")
        item.status = AdaptiveEvChargeWindowStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveEvChargeWindowStatus.IDLE) }
    }
}

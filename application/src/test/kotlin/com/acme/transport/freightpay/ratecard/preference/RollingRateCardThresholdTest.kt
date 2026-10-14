package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRateCardThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newRollingRateCardThreshold("code-114", "label-525")
        assertEquals(RollingRateCardThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingRateCardThreshold("code-114", "label-525")
        try {
            item.moveTo(RollingRateCardThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRateCardThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingRateCardThreshold("code-114", "label-525")
        item.status = RollingRateCardThresholdStatus.FAILED
        assertFails("move") { item.moveTo(RollingRateCardThresholdStatus.NEW) }
    }
}

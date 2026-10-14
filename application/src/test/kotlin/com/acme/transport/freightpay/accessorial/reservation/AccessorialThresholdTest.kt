package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newAccessorialThreshold("code-739", "name-504")
        assertEquals(AccessorialThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAccessorialThreshold("code-739", "name-504")
        try {
            item.moveTo(AccessorialThresholdStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialThresholdStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAccessorialThreshold("code-739", "name-504")
        item.status = AccessorialThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(AccessorialThresholdStatus.PLANNED) }
    }
}

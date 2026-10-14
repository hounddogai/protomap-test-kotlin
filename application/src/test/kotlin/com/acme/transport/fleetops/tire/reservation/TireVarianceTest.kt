package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newTireVariance("code-81", "label-722")
        assertEquals(TireVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTireVariance("code-81", "label-722")
        try {
            item.moveTo(TireVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTireVariance("code-81", "label-722")
        item.status = TireVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(TireVarianceStatus.IDLE) }
    }
}

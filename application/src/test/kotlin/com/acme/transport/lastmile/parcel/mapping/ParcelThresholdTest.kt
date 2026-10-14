package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newParcelThreshold("code-81", "label-988")
        assertEquals(ParcelThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newParcelThreshold("code-81", "label-988")
        try {
            item.moveTo(ParcelThresholdStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelThresholdStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newParcelThreshold("code-81", "label-988")
        item.status = ParcelThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(ParcelThresholdStatus.IDLE) }
    }
}

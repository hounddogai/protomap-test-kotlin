package com.acme.transport.lastmile.parcel.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalParcelWindow("code-771", "title-412")
        assertEquals(RegionalParcelWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalParcelWindow("code-771", "title-412")
        try {
            item.moveTo(RegionalParcelWindowStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelWindowStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalParcelWindow("code-771", "title-412")
        item.status = RegionalParcelWindowStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalParcelWindowStatus.IDLE) }
    }
}

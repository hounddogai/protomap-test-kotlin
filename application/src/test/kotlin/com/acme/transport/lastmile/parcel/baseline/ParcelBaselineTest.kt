package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newParcelBaseline("code-641", "label-731")
        assertEquals(ParcelBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newParcelBaseline("code-641", "label-731")
        try {
            item.moveTo(ParcelBaselineStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelBaselineStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newParcelBaseline("code-641", "label-731")
        item.status = ParcelBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(ParcelBaselineStatus.PLANNED) }
    }
}

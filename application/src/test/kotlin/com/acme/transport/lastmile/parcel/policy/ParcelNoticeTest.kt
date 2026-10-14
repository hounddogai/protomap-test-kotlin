package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newParcelNotice("code-340", "label-314")
        assertEquals(ParcelNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newParcelNotice("code-340", "label-314")
        try {
            item.moveTo(ParcelNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newParcelNotice("code-340", "label-314")
        item.status = ParcelNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(ParcelNoticeStatus.PLANNED) }
    }
}

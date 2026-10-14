package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelTargetTest {
    @Test
    fun `starts in new`() {
        val item = newParcelTarget("code-628", "title-439")
        assertEquals(ParcelTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newParcelTarget("code-628", "title-439")
        try {
            item.moveTo(ParcelTargetStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelTargetStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newParcelTarget("code-628", "title-439")
        item.status = ParcelTargetStatus.FAILED
        assertFails("move") { item.moveTo(ParcelTargetStatus.NEW) }
    }
}

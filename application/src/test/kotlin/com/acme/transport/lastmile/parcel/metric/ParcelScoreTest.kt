package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelScoreTest {
    @Test
    fun `starts in open`() {
        val item = newParcelScore("code-842", "title-544")
        assertEquals(ParcelScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newParcelScore("code-842", "title-544")
        try {
            item.moveTo(ParcelScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newParcelScore("code-842", "title-544")
        item.status = ParcelScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(ParcelScoreStatus.OPEN) }
    }
}

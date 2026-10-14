package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTenderRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyTenderRecord("code-528", "title-773")
        assertEquals(LegacyTenderRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyTenderRecord("code-528", "title-773")
        try {
            item.moveTo(LegacyTenderRecordStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTenderRecordStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyTenderRecord("code-528", "title-773")
        item.status = LegacyTenderRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyTenderRecordStatus.PLANNED) }
    }
}

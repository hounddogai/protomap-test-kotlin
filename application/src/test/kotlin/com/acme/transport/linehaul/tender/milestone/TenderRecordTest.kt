package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderRecord("code-468", "title-813")
        assertEquals(TenderRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTenderRecord("code-468", "title-813")
        try {
            item.moveTo(TenderRecordStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderRecordStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderRecord("code-468", "title-813")
        item.status = TenderRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderRecordStatus.PLANNED) }
    }
}

package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderEntryTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryTenderEntry("code-811", "title-818")
        assertEquals(PrimaryTenderEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryTenderEntry("code-811", "title-818")
        try {
            item.moveTo(PrimaryTenderEntryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderEntryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryTenderEntry("code-811", "title-818")
        item.status = PrimaryTenderEntryStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryTenderEntryStatus.NEW) }
    }
}

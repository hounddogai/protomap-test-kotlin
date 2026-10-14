package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderGroup("code-255", "title-461")
        assertEquals(TenderGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTenderGroup("code-255", "title-461")
        try {
            item.moveTo(TenderGroupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderGroupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderGroup("code-255", "title-461")
        item.status = TenderGroupStatus.STOPPED
        assertFails("move") { item.moveTo(TenderGroupStatus.IDLE) }
    }
}

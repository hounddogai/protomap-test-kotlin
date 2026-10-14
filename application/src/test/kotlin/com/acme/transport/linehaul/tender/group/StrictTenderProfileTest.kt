package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTenderProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictTenderProfile("code-849", "label-856")
        assertEquals(StrictTenderProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictTenderProfile("code-849", "label-856")
        try {
            item.moveTo(StrictTenderProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTenderProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictTenderProfile("code-849", "label-856")
        item.status = StrictTenderProfileStatus.CLOSED
        assertFails("move") { item.moveTo(StrictTenderProfileStatus.DRAFT) }
    }
}

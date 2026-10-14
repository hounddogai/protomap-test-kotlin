package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTenderGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientTenderGroup("code-567", "title-895")
        assertEquals(LenientTenderGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLenientTenderGroup("code-567", "title-895")
        try {
            item.moveTo(LenientTenderGroupStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTenderGroupStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientTenderGroup("code-567", "title-895")
        item.status = LenientTenderGroupStatus.CLOSED
        assertFails("move") { item.moveTo(LenientTenderGroupStatus.DRAFT) }
    }
}

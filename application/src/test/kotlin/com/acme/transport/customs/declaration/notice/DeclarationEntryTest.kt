package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationEntry("code-651", "title-85")
        assertEquals(DeclarationEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDeclarationEntry("code-651", "title-85")
        try {
            item.moveTo(DeclarationEntryStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationEntryStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationEntry("code-651", "title-85")
        item.status = DeclarationEntryStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationEntryStatus.IDLE) }
    }
}

package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newDeclarationSlot("code-276", "title-154")
        assertEquals(DeclarationSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDeclarationSlot("code-276", "title-154")
        try {
            item.moveTo(DeclarationSlotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationSlotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDeclarationSlot("code-276", "title-154")
        item.status = DeclarationSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DeclarationSlotStatus.PROPOSED) }
    }
}

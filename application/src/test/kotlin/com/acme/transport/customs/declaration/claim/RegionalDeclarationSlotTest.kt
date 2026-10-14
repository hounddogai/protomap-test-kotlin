package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDeclarationSlotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalDeclarationSlot("code-323", "label-822")
        assertEquals(RegionalDeclarationSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalDeclarationSlot("code-323", "label-822")
        try {
            item.moveTo(RegionalDeclarationSlotStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDeclarationSlotStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalDeclarationSlot("code-323", "label-822")
        item.status = RegionalDeclarationSlotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalDeclarationSlotStatus.NEW) }
    }
}

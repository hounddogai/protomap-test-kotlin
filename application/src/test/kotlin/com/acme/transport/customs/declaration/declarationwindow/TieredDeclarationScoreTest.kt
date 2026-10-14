package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDeclarationScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredDeclarationScore("code-650", "label-48")
        assertEquals(TieredDeclarationScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTieredDeclarationScore("code-650", "label-48")
        try {
            item.moveTo(TieredDeclarationScoreStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDeclarationScoreStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredDeclarationScore("code-650", "label-48")
        item.status = TieredDeclarationScoreStatus.CLOSED
        assertFails("move") { item.moveTo(TieredDeclarationScoreStatus.DRAFT) }
    }
}

package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDeclarationProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDeclarationProfile("code-281", "title-869")
        assertEquals(TieredDeclarationProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredDeclarationProfile("code-281", "title-869")
        try {
            item.moveTo(TieredDeclarationProfileStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDeclarationProfileStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDeclarationProfile("code-281", "title-869")
        item.status = TieredDeclarationProfileStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDeclarationProfileStatus.IDLE) }
    }
}

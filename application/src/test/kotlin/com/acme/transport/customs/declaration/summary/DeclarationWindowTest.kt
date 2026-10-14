package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationWindowTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationWindow("code-717", "name-910")
        assertEquals(DeclarationWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDeclarationWindow("code-717", "name-910")
        try {
            item.moveTo(DeclarationWindowStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationWindowStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationWindow("code-717", "name-910")
        item.status = DeclarationWindowStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationWindowStatus.NEW) }
    }
}

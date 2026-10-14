package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newDeclarationLedger("code-328", "title-604")
        assertEquals(DeclarationLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDeclarationLedger("code-328", "title-604")
        try {
            item.moveTo(DeclarationLedgerStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationLedgerStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDeclarationLedger("code-328", "title-604")
        item.status = DeclarationLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(DeclarationLedgerStatus.REQUESTED) }
    }
}

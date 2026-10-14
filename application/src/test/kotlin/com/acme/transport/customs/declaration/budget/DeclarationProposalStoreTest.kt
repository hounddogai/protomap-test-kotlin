package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationProposalStore()
        val item = newDeclarationProposal("code-897", "name-594")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

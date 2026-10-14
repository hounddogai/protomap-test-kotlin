package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationAllocationStore()
        val item = newDeclarationAllocation("code-952", "name-816")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

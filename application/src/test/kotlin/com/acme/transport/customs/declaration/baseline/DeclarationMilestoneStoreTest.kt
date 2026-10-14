package com.acme.transport.customs.declaration.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationMilestoneStore()
        val item = newDeclarationMilestone("code-638", "label-942")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

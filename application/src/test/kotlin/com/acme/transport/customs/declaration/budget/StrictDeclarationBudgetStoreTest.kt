package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDeclarationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDeclarationBudgetStore()
        val item = newStrictDeclarationBudget("code-460", "label-778")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

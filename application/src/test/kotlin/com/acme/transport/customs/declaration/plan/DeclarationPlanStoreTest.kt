package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationPlanStore()
        val item = newDeclarationPlan("code-341", "name-599")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

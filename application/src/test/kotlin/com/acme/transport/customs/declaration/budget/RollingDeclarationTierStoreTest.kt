package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDeclarationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDeclarationTierStore()
        val item = newRollingDeclarationTier("code-63", "name-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

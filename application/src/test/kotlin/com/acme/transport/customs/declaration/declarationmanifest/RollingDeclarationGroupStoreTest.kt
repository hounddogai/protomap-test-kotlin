package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDeclarationGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDeclarationGroupStore()
        val item = newRollingDeclarationGroup("code-13", "title-914")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDeclarationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDeclarationRecordStore()
        val item = newDefaultDeclarationRecord("code-91", "label-41")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDeclarationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDeclarationRecordStore()
        val item = newRollingDeclarationRecord("code-3", "title-961")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

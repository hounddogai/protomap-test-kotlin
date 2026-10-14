package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationCheckpointStore()
        val item = newDeclarationCheckpoint("code-697", "title-488")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

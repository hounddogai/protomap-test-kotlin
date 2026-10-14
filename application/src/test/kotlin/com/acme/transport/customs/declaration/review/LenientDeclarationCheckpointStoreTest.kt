package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDeclarationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDeclarationCheckpointStore()
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

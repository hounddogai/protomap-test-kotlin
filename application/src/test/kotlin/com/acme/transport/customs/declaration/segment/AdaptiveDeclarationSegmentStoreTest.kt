package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDeclarationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDeclarationSegmentStore()
        val item = newAdaptiveDeclarationSegment("code-846", "name-151")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

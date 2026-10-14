package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDeclarationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDeclarationReviewStore()
        val item = newFallbackDeclarationReview("code-37", "name-88")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

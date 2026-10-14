package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDeclarationMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDeclarationMetricStore()
        val item = newLenientDeclarationMetric("code-486", "title-793")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

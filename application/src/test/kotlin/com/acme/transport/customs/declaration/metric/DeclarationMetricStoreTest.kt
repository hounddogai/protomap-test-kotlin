package com.acme.transport.customs.declaration.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationMetricStore()
        val item = newDeclarationMetric("code-631", "title-764")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

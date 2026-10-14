package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTenderTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTenderTemplateStore()
        val item = newDefaultTenderTemplate("code-784", "name-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

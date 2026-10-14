package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTenderTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTenderTemplateStore()
        val item = newCappedTenderTemplate("code-12", "label-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

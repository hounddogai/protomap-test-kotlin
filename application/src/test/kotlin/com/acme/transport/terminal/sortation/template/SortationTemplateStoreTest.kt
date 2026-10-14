package com.acme.transport.terminal.sortation.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationTemplateStore()
        val item = newSortationTemplate("code-101", "label-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

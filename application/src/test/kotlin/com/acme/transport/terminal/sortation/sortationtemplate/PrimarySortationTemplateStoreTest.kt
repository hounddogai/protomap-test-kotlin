package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimarySortationTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimarySortationTemplateStore()
        val item = newPrimarySortationTemplate("code-599", "title-620")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

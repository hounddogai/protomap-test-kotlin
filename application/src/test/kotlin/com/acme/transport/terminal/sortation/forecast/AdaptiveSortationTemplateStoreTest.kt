package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveSortationTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveSortationTemplateStore()
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

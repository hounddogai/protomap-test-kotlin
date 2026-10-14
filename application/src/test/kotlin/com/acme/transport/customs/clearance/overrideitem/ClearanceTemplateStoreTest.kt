package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceTemplateStore()
        val item = newClearanceTemplate("code-256", "title-71")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalInspectionWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalInspectionWindowStore()
        val item = newGlobalInspectionWindow("code-995", "title-717")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

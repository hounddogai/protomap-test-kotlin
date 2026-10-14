package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionWindowStore()
        val item = newInspectionWindow("code-533", "label-140")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

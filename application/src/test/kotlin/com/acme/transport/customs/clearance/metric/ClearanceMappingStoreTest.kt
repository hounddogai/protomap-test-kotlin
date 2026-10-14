package com.acme.transport.customs.clearance.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceMappingStore()
        val item = newClearanceMapping("code-913", "title-322")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

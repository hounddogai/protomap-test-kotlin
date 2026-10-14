package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceBaselineStore()
        val item = newClearanceBaseline("code-483", "title-632")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

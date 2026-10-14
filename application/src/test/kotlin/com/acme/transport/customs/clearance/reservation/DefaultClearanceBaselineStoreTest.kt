package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultClearanceBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultClearanceBaselineStore()
        val item = newDefaultClearanceBaseline("code-763", "name-393")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

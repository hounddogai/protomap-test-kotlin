package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyClearanceWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyClearanceWindowStore()
        val item = newLegacyClearanceWindow("code-810", "label-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

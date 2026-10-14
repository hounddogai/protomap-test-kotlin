package com.acme.transport.linehaul.capacity.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityQuotaStore()
        val item = newCapacityQuota("code-125", "title-313")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

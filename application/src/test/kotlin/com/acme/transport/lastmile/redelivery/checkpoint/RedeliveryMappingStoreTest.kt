package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryMappingStore()
        val item = newRedeliveryMapping("code-907", "name-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRedeliveryEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRedeliveryEntryStore()
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

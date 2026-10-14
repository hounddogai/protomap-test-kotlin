package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRestBreakEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRestBreakEntryStore()
        val item = newRegionalRestBreakEntry("code-728", "name-475")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

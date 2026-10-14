package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCapacityManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCapacityManifestStore()
        val item = newRegionalCapacityManifest("code-9", "title-394")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

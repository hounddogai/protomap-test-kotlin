package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTireBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTireBatchStore()
        val item = newRegionalTireBatch("code-341", "label-681")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

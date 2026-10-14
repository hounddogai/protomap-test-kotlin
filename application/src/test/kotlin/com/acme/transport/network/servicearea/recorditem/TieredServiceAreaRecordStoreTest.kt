package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredServiceAreaRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredServiceAreaRecordStore()
        val item = newTieredServiceAreaRecord("code-876", "name-541")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

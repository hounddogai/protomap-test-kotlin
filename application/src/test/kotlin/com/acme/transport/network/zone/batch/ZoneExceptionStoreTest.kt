package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneExceptionStore()
        val item = newZoneException("code-4", "label-384")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

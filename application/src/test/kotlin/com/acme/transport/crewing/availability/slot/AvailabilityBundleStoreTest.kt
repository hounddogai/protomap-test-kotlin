package com.acme.transport.crewing.availability.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityBundleStore()
        val item = newAvailabilityBundle("code-887", "label-631")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

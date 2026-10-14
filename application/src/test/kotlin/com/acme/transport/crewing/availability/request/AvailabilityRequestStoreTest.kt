package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityRequestStore()
        val item = newAvailabilityRequest("code-524", "label-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

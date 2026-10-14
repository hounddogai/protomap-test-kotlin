package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityProfileStore()
        val item = newAvailabilityProfile("code-498", "title-822")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

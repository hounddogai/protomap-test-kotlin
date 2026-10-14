package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyAvailabilityExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyAvailabilityExceptionStore()
        val item = newLegacyAvailabilityException("code-982", "name-315")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

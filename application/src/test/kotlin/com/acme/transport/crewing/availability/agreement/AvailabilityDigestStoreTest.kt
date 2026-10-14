package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityDigestStore()
        val item = newAvailabilityDigest("code-954", "label-379")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

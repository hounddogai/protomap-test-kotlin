package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyAvailabilityDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyAvailabilityDigestStore()
        val item = newLegacyAvailabilityDigest("code-150", "name-223")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

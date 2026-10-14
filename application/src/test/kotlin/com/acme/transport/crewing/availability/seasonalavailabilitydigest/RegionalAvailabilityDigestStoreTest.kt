package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAvailabilityDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAvailabilityDigestStore()
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

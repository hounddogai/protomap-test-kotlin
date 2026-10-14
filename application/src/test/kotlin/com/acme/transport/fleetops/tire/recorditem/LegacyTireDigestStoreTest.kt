package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTireDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTireDigestStore()
        val item = newLegacyTireDigest("code-640", "label-692")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

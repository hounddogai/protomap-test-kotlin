package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireDigestStore()
        val item = newTireDigest("code-386", "name-625")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

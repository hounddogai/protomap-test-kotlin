package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTireManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTireManifestStore()
        val item = newDefaultTireManifest("code-551", "title-445")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackClearanceManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackClearanceManifestStore()
        val item = newFallbackClearanceManifest("code-289", "label-187")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

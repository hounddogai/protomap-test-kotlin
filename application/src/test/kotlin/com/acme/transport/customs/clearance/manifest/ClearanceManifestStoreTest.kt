package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceManifestStore()
        val item = newClearanceManifest("code-760", "name-387")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

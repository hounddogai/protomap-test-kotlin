package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedClearanceBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedClearanceBundleStore()
        val item = newCappedClearanceBundle("code-297", "label-514")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

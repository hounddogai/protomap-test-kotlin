package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCutoffManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCutoffManifestStore()
        val item = newLenientCutoffManifest("code-358", "name-46")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

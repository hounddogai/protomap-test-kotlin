package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalParcelManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalParcelManifestStore()
        val item = newGlobalParcelManifest("code-172", "title-178")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

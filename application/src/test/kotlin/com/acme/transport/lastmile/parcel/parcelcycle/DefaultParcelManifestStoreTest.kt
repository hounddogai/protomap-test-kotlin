package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultParcelManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultParcelManifestStore()
        val item = newDefaultParcelManifest("code-917", "label-625")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

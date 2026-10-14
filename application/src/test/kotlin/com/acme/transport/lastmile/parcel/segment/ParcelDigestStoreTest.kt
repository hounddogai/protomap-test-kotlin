package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelDigestStore()
        val item = newParcelDigest("code-556", "title-262")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

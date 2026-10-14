package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledParcelDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledParcelDigestStore()
        val item = newScheduledParcelDigest("code-931", "label-861")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

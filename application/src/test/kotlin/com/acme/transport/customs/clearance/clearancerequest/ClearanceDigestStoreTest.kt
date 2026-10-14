package com.acme.transport.customs.clearance.clearancerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceDigestStore()
        val item = newClearanceDigest("code-329", "title-667")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

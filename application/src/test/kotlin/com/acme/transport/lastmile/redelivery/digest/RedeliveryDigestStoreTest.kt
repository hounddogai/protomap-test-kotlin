package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryDigestStore()
        val item = newRedeliveryDigest("code-651", "title-995")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

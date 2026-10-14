package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeDigestStore()
        val item = newDisputeDigest("code-540", "title-579")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

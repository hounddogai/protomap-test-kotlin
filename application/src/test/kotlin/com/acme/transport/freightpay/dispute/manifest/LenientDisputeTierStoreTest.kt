package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDisputeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDisputeTierStore()
        val item = newLenientDisputeTier("code-546", "title-404")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

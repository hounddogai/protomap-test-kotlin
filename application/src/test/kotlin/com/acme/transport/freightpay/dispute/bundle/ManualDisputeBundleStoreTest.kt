package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDisputeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDisputeBundleStore()
        val item = newManualDisputeBundle("code-509", "label-303")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

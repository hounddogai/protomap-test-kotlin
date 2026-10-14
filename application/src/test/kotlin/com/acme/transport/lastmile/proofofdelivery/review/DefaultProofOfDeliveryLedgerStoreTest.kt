package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultProofOfDeliveryLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultProofOfDeliveryLedgerStore()
        val item = newDefaultProofOfDeliveryLedger("code-414", "title-423")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

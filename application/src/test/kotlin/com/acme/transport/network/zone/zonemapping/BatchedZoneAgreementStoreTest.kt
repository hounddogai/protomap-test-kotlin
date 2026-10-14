package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedZoneAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedZoneAgreementStore()
        val item = newBatchedZoneAgreement("code-456", "label-377")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

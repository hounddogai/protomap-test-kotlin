package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDisputeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDisputeAgreementStore()
        val item = newLenientDisputeAgreement("code-18", "name-735")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

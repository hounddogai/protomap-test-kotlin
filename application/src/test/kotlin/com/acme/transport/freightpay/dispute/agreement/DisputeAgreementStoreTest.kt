package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeAgreementStore()
        val item = newDisputeAgreement("code-727", "title-140")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

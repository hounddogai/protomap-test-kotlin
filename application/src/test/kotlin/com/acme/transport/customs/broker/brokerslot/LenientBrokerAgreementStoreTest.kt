package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientBrokerAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientBrokerAgreementStore()
        val item = newLenientBrokerAgreement("code-249", "title-500")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

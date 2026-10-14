package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffAgreementStore()
        val item = newDefaultCutoffAgreement("code-555", "title-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

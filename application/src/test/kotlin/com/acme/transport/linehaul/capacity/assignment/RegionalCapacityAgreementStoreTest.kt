package com.acme.transport.linehaul.capacity.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCapacityAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCapacityAgreementStore()
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

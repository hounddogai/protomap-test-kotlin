package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalEvChargeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalEvChargeAgreementStore()
        val item = newRegionalEvChargeAgreement("code-545", "label-832")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

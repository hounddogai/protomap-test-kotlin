package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredEvChargeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredEvChargeAgreementStore()
        val item = newTieredEvChargeAgreement("code-322", "name-171")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

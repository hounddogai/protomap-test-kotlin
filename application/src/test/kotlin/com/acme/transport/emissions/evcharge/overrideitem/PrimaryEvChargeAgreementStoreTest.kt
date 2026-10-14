package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryEvChargeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryEvChargeAgreementStore()
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

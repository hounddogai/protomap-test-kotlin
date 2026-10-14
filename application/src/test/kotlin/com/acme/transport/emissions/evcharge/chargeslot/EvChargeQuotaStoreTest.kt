package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeQuotaStore()
        val item = newEvChargeQuota("code-737", "title-423")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

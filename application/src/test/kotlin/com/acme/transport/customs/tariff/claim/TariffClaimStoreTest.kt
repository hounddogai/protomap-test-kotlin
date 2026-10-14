package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffClaimStore()
        val item = newTariffClaim("code-788", "label-435")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

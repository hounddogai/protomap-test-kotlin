package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTariffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTariffClaimStore()
        val item = newCappedTariffClaim("code-72", "name-676")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

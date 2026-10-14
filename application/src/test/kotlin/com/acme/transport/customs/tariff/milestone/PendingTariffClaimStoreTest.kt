package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTariffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTariffClaimStore()
        val item = newPendingTariffClaim("code-661", "label-703")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

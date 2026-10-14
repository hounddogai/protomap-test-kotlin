package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDisputeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDisputeTierStore()
        val item = newLegacyDisputeTier("code-349", "title-201")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

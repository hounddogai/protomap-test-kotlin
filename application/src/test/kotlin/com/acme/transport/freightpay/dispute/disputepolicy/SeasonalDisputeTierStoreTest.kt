package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDisputeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDisputeTierStore()
        val item = newSeasonalDisputeTier("code-422", "title-938")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

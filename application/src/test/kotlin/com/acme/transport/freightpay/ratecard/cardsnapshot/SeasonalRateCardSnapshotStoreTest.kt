package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRateCardSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRateCardSnapshotStore()
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

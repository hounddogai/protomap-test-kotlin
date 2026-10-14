package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRateCardDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRateCardDigestStore()
        val item = newSeasonalRateCardDigest("code-591", "label-14")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

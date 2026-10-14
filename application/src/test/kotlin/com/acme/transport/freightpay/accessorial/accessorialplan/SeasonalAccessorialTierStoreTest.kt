package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAccessorialTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAccessorialTierStore()
        val item = newSeasonalAccessorialTier("code-690", "label-136")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

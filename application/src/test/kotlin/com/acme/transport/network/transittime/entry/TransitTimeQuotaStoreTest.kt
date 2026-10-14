package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeQuotaStore()
        val item = newTransitTimeQuota("code-208", "title-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

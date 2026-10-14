package com.acme.transport.network.servicearea.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaLedgerStore()
        val item = newServiceAreaLedger("code-960", "title-66")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

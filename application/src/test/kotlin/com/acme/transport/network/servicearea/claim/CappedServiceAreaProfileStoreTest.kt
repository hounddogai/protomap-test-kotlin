package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedServiceAreaProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedServiceAreaProfileStore()
        val item = newCappedServiceAreaProfile("code-787", "name-382")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

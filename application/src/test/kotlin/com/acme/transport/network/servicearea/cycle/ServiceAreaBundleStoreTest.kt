package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaBundleStore()
        val item = newServiceAreaBundle("code-757", "title-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

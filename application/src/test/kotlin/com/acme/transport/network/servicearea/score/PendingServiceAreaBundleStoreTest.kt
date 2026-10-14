package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingServiceAreaBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingServiceAreaBundleStore()
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

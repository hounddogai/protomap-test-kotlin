package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveBackhaulQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveBackhaulQuotaStore()
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaQuotaStore()
        val item = newServiceAreaQuota("code-155", "title-257")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

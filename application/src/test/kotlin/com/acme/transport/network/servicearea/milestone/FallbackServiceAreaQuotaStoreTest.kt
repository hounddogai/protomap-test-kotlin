package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackServiceAreaQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackServiceAreaQuotaStore()
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

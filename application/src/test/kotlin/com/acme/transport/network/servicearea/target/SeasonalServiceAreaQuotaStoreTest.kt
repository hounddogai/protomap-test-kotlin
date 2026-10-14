package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServiceAreaQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServiceAreaQuotaStore()
        val item = newSeasonalServiceAreaQuota("code-931", "label-265")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

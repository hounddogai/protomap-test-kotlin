package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyHubRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyHubRecordStore()
        val item = newLegacyHubRecord("code-452", "title-373")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

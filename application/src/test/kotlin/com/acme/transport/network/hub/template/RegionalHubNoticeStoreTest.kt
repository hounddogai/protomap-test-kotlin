package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalHubNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalHubNoticeStore()
        val item = newRegionalHubNotice("code-798", "label-883")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

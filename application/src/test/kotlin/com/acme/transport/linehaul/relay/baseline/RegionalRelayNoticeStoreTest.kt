package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRelayNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRelayNoticeStore()
        val item = newRegionalRelayNotice("code-364", "name-937")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

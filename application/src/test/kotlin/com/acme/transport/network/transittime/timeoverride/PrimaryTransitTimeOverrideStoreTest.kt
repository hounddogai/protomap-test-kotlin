package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTransitTimeOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTransitTimeOverrideStore()
        val item = newPrimaryTransitTimeOverride("code-99", "title-59")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

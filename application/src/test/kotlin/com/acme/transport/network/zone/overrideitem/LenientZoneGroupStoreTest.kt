package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientZoneGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientZoneGroupStore()
        val item = newLenientZoneGroup("code-740", "name-882")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

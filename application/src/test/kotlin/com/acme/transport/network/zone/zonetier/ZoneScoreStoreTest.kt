package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneScoreStore()
        val item = newZoneScore("code-666", "title-724")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

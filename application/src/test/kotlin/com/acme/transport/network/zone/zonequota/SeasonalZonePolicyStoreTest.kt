package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalZonePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalZonePolicyStore()
        val item = newSeasonalZonePolicy("code-646", "name-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

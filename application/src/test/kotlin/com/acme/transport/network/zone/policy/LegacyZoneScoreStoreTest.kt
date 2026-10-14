package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyZoneScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyZoneScoreStore()
        val item = newLegacyZoneScore("code-626", "title-46")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

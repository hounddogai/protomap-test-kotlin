package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalZoneTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalZoneTargetStore()
        val item = newGlobalZoneTarget("code-917", "title-215")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropPreferenceStore()
        val item = newDropPreference("code-812", "label-973")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

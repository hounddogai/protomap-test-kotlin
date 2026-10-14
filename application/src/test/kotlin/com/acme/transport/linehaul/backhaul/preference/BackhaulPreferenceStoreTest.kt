package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulPreferenceStore()
        val item = newBackhaulPreference("code-393", "name-147")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

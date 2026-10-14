package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDwellPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDwellPreferenceStore()
        val item = newFallbackDwellPreference("code-492", "name-171")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

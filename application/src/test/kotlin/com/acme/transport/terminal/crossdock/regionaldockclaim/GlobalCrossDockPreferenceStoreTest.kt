package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCrossDockPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCrossDockPreferenceStore()
        val item = newGlobalCrossDockPreference("code-512", "label-174")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

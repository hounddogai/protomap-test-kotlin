package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCrossDockPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCrossDockPreferenceStore()
        val item = newDefaultCrossDockPreference("code-15", "name-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

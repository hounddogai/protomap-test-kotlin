package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDwellManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDwellManifestStore()
        val item = newManualDwellManifest("code-758", "name-571")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorManifestStore()
        val item = newDockDoorManifest("code-258", "label-831")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

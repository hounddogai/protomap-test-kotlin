package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterManifestStore()
        val item = newRosterManifest("code-235", "title-754")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

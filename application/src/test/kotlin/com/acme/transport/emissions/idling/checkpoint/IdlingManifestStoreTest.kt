package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingManifestStore()
        val item = newIdlingManifest("code-66", "title-10")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

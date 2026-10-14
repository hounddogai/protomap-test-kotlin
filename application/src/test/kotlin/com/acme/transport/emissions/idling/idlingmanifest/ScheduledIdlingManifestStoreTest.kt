package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledIdlingManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledIdlingManifestStore()
        val item = newScheduledIdlingManifest("code-122", "title-616")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

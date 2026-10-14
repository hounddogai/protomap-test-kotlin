package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingManifestStore()
        val item = newCoachingManifest("code-169", "name-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

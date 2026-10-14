package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissManifestStore()
        val item = newNearMissManifest("code-431", "name-836")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

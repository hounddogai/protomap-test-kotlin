package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationManifestStore()
        val item = newViolationManifest("code-977", "name-508")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

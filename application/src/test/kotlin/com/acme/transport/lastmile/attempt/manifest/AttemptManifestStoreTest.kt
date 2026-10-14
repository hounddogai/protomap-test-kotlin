package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptManifestStore()
        val item = newAttemptManifest("code-386", "name-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

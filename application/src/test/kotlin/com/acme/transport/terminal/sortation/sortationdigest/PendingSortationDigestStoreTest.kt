package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingSortationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingSortationDigestStore()
        val item = newPendingSortationDigest("code-515", "label-766")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

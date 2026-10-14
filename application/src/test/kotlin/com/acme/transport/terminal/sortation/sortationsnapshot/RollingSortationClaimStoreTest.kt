package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSortationClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSortationClaimStore()
        val item = newRollingSortationClaim("code-660", "label-444")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

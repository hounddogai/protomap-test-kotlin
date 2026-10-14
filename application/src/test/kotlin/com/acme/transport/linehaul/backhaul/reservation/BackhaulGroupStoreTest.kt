package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulGroupStore()
        val item = newBackhaulGroup("code-473", "title-84")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

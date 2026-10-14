package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDropProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDropProfileStore()
        val item = newPrimaryDropProfile("code-947", "name-10")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

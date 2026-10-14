package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryIdlingProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryIdlingProfileStore()
        val item = newPrimaryIdlingProfile("code-421", "title-943")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

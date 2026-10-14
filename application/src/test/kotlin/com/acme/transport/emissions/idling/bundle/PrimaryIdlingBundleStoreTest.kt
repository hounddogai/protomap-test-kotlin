package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryIdlingBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryIdlingBundleStore()
        val item = newPrimaryIdlingBundle("code-111", "label-317")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

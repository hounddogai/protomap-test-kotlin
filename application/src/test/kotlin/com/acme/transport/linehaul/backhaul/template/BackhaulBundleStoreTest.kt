package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulBundleStore()
        val item = newBackhaulBundle("code-541", "label-602")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}

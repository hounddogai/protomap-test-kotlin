package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBackhaulWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBackhaulWindowStore()
        val item = newCappedBackhaulWindow("code-100", "title-389")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
